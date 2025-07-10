package team03.mopl.domain.curation.service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import jakarta.annotation.PostConstruct;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.curation.KeywordDeleteDeniedException;
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.entity.KeywordContent;
import team03.mopl.domain.curation.repository.KeywordContentRepository;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CurationServiceImpl implements CurationService {
  private final ReviewService reviewService;
  private final ContentRepository contentRepository;
  private final KeywordRepository keywordRepository;
  private final KeywordContentRepository keywordContentRepository;
  private final UserRepository userRepository;

  private StanfordCoreNLP nlpPipeline;
  private Komoran komoran;

  // 언어별 패턴 정의
  private static final Pattern KOREAN_PATTERN = Pattern.compile("[가-힣]+");
  private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+");

  // 한국어 불용어 (확장)
  private static final Set<String> KOREAN_STOPWORDS = Set.of(
      "이", "그", "저", "것", "들", "에", "는", "을", "를", "의", "가", "에서", "로", "으로",
      "와", "과", "하다", "되다", "있다", "없다", "같다", "다른", "새로운", "좋은", "나쁜",
      "이런", "저런", "그런", "어떤", "무엇", "누구", "언제", "어디서", "왜", "어떻게"
  );

  // 영어 불용어
  private static final Set<String> ENGLISH_STOPWORDS = Set.of(
      "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
      "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did"
  );

  // 한국어 동의어 사전 (확장 가능)
  private static final Map<String, Set<String>> SYNONYM_MAP = Map.of(
      "영화", Set.of("무비", "시네마", "필름", "movie", "film"),
      "드라마", Set.of("연속극", "시리즈", "TV드라마", "drama", "series"),
      "음악", Set.of("뮤직", "노래", "곡", "music", "song"),
      "스포츠", Set.of("운동", "체육", "경기", "sports", "game"),
      "게임", Set.of("놀이", "경기", "게이밍", "game", "gaming"),
      "코미디", Set.of("개그", "웃긴", "유머", "comedy", "funny"),
      "액션", Set.of("액숀", "격투", "전투", "action", "fight"),
      "로맨스", Set.of("사랑", "연애", "romance", "love"),
      "공포", Set.of("호러", "무서운", "horror", "scary"),
      "판타지", Set.of("환상", "마법", "fantasy", "magic")
  );

  // 한영 번역 사전 (확장)
  private static final Map<String, String> TRANSLATION_MAP = Map.ofEntries(
      Map.entry("액션", "action"),
      Map.entry("드라마", "drama"),
      Map.entry("코미디", "comedy"),
      Map.entry("로맨스", "romance"),
      Map.entry("공포", "horror"),
      Map.entry("스릴러", "thriller"),
      Map.entry("판타지", "fantasy"),
      Map.entry("음악", "music"),
      Map.entry("영화", "movie"),
      Map.entry("스포츠", "sports"),
      Map.entry("축구", "football"),
      Map.entry("농구", "basketball"),
      Map.entry("야구", "baseball"),
      Map.entry("한국", "korean"),
      Map.entry("미국", "american"),
      Map.entry("일본", "japanese"),
      Map.entry("중국", "chinese")
  );

  @PostConstruct
  @Override
  public void init() {
    initializeAI();
    initializeKomoran();
    log.info("init - 큐레이션 서비스 초기화 완료 (Komoran + 규칙 기반 모드)");
  }

  // Stanford NLP 초기화
  private void initializeAI() {
    try {
      Properties props = new Properties();
      props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
      props.setProperty("ner.useSUTime", "false");
      this.nlpPipeline = new StanfordCoreNLP(props);
      log.info("initializeAI - Stanford CoreNLP 초기화 완료");
    } catch (Exception e) {
      log.warn("Stanford NLP 초기화 실패: {}", e.getMessage());
      this.nlpPipeline = null;
    }
  }

  // Komoran 형태소 분석기 초기화
  private void initializeKomoran() {
    try {
      this.komoran = new Komoran(DEFAULT_MODEL.FULL);
      log.info("initializeKomoran - Komoran 형태소 분석기 초기화 완료");
    } catch (Exception e) {
      log.warn("Komoran 초기화 실패: {}", e.getMessage());
      this.komoran = null;
    }
  }

  // 사용자 키워드 등록
  @Override
  @Transactional
  public KeywordDto registerKeyword(UUID userId, String keywordText) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    String normalizedKeyword = normalizeMultilingualText(keywordText);
    String language = detectLanguage(keywordText);

    Keyword keyword = Keyword.builder()
        .user(user)
        .keyword(normalizedKeyword)
        .build();
    keyword = keywordRepository.save(keyword);

    List<ContentDto> recommendations = curateContentForKeyword(keyword);

    log.info("registerKeyword - 키워드 등록: '{}' -> '{}' [{}] (매칭된 콘텐츠: {}개)",
        keywordText, normalizedKeyword, language, recommendations.size());
    return KeywordDto.from(keyword);
  }

  // AI 기반 키워드별 콘텐츠 큐레이션
  @Override
  @Transactional
  public List<ContentDto> curateContentForKeyword(Keyword keyword) {
    String keywordText = keyword.getKeyword();
    List<Content> recommendations = new ArrayList<>();
    List<Content> allContents = contentRepository.findAll();

    for (Content content : allContents) {
      double aiScore = calculateAdvancedMatchingScore(keywordText, content);
      double threshold = getLanguageBasedThreshold(keywordText);

      if (aiScore > threshold) {
        recommendations.add(content);
        KeywordContent keywordContent = new KeywordContent(keyword, content);
        keywordContentRepository.save(keywordContent);
      }
    }

    recommendations.sort((c1, c2) -> {
      double score1 = calculateAdvancedMatchingScore(keywordText, c1);
      double score2 = calculateAdvancedMatchingScore(keywordText, c2);
      return Double.compare(score2, score1);
    });

    return recommendations.stream().map(ContentDto::from).toList();
  }

  // 개선된 AI 매칭 점수 계산
  private double calculateAdvancedMatchingScore(String keyword, Content content) {
    double totalScore = 0.0;

    // 1. 제목 매칭 (가중치 증가)
    double titleScore = calculateAdvancedTextSimilarity(keyword, content.getTitle()) * 0.5;

    // 2. 설명 매칭
    double descScore = calculateAdvancedTextSimilarity(keyword, content.getDescription()) * 0.3;

    // 3. 장르/타입 매칭
    double typeScore = calculateMultilingualTypeMatch(keyword, content.getContentType()) * 0.15;

    // 4. 평점 보너스
    double ratingBonus = content.getAvgRating() != null ?
        content.getAvgRating().doubleValue() / 10.0 * 0.05 : 0.0;

    totalScore = titleScore + descScore + typeScore + ratingBonus;
    return Math.min(totalScore, 1.0);
  }

  // 개선된 텍스트 유사도 계산
  private double calculateAdvancedTextSimilarity(String keyword, String text) {
    if (text == null || keyword == null) return 0.0;

    keyword = normalizeMultilingualText(keyword);
    text = normalizeMultilingualText(text);

    // 정확한 매칭
    if (text.contains(keyword)) return 1.0;

    // 1. 형태소 기반 매칭
    double morphemeScore = calculateMorphemeSimilarity(keyword, text);

    // 2. 동의어 매칭
    double synonymScore = calculateSynonymSimilarity(keyword, text);

    // 3. 번역어 매칭
    double translationScore = calculateTranslationSimilarity(keyword, text);

    // 4. 기본 단어 매칭
    double basicScore = calculateBasicWordSimilarity(keyword, text);

    // 가중 평균
    return Math.min(
        morphemeScore * 0.4 +
            synonymScore * 0.3 +
            translationScore * 0.2 +
            basicScore * 0.1,
        1.0
    );
  }

  // 형태소 기반 유사도 계산
  private double calculateMorphemeSimilarity(String keyword, String text) {
    if (komoran == null) {
      return calculateBasicWordSimilarity(keyword, text);
    }

    try {
      List<String> keywordMorphemes = extractMeaningfulMorphemes(keyword);
      List<String> textMorphemes = extractMeaningfulMorphemes(text);

      if (keywordMorphemes.isEmpty() || textMorphemes.isEmpty()) {
        return 0.0;
      }

      // Jaccard 유사도 계산
      Set<String> keywordSet = new HashSet<>(keywordMorphemes);
      Set<String> textSet = new HashSet<>(textMorphemes);

      Set<String> intersection = new HashSet<>(keywordSet);
      intersection.retainAll(textSet);

      Set<String> union = new HashSet<>(keywordSet);
      union.addAll(textSet);

      return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();

    } catch (Exception e) {
      log.warn("형태소 분석 실패: {}", e.getMessage());
      return calculateBasicWordSimilarity(keyword, text);
    }
  }

  // 의미있는 형태소 추출
  private List<String> extractMeaningfulMorphemes(String text) {
    if (komoran == null || text == null || text.trim().isEmpty()) {
      return Arrays.asList(text.split("\\s+"));
    }

    try {
      KomoranResult result = komoran.analyze(text);
      return result.getMorphesByTags("NNG", "NNP", "VV", "VA", "SL") // 명사, 동사, 형용사, 외국어
          .stream()
          .filter(morph -> morph.length() > 1) // 한 글자 제외
          .filter(morph -> !KOREAN_STOPWORDS.contains(morph))
          .distinct()
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.warn("형태소 추출 실패: {}", e.getMessage());
      return Arrays.asList(text.split("\\s+"));
    }
  }

  // 동의어 유사도 계산
  private double calculateSynonymSimilarity(String keyword, String text) {
    String[] keywordWords = keyword.split("\\s+");
    String[] textWords = text.split("\\s+");

    int matches = 0;
    for (String kWord : keywordWords) {
      for (String tWord : textWords) {
        if (areSynonyms(kWord, tWord)) {
          matches++;
          break;
        }
      }
    }

    return keywordWords.length > 0 ? (double) matches / keywordWords.length : 0.0;
  }

  // 동의어 판별
  private boolean areSynonyms(String word1, String word2) {
    if (word1.equals(word2)) return true;

    return SYNONYM_MAP.entrySet().stream()
        .anyMatch(entry ->
            (entry.getKey().equals(word1) && entry.getValue().contains(word2)) ||
                (entry.getKey().equals(word2) && entry.getValue().contains(word1)) ||
                (entry.getValue().contains(word1) && entry.getValue().contains(word2))
        );
  }

  // 번역어 유사도 계산
  private double calculateTranslationSimilarity(String keyword, String text) {
    String[] keywordWords = keyword.split("\\s+");
    String[] textWords = text.split("\\s+");

    int matches = 0;
    for (String kWord : keywordWords) {
      for (String tWord : textWords) {
        if (areTranslations(kWord, tWord)) {
          matches++;
          break;
        }
      }
    }

    return keywordWords.length > 0 ? (double) matches / keywordWords.length : 0.0;
  }

  // 번역어 판별
  private boolean areTranslations(String word1, String word2) {
    return (TRANSLATION_MAP.containsKey(word1) && TRANSLATION_MAP.get(word1).equals(word2)) ||
        (TRANSLATION_MAP.containsKey(word2) && TRANSLATION_MAP.get(word2).equals(word1));
  }

  // 기본 단어 유사도 계산
  private double calculateBasicWordSimilarity(String keyword, String text) {
    String[] keywordWords = keyword.split("\\s+");
    String[] textWords = text.split("\\s+");

    int exactMatches = 0;
    int partialMatches = 0;

    for (String kWord : keywordWords) {
      for (String tWord : textWords) {
        if (kWord.equals(tWord)) {
          exactMatches++;
          break;
        } else if (kWord.length() > 2 && tWord.length() > 2 &&
            (kWord.contains(tWord) || tWord.contains(kWord))) {
          partialMatches++;
          break;
        }
      }
    }

    double exactScore = (double) exactMatches / keywordWords.length * 0.8;
    double partialScore = (double) partialMatches / keywordWords.length * 0.4;

    return Math.min(exactScore + partialScore, 1.0);
  }

  // 언어 감지
  private String detectLanguage(String text) {
    if (text == null || text.trim().isEmpty()) {
      return "unknown";
    }

    boolean hasKorean = KOREAN_PATTERN.matcher(text).find();
    boolean hasEnglish = ENGLISH_PATTERN.matcher(text).find();

    if (hasKorean && hasEnglish) {
      return "mixed";
    } else if (hasKorean) {
      return "korean";
    } else if (hasEnglish) {
      return "english";
    } else {
      return "other";
    }
  }

  // 언어별 임계값 설정
  private double getLanguageBasedThreshold(String text) {
    String language = detectLanguage(text);
    switch (language) {
      case "korean":
        return 0.2; // 한국어는 임계값 낮춤 (형태소 분석 활용)
      case "english":
        return 0.3;
      case "mixed":
        return 0.25;
      default:
        return 0.3;
    }
  }

  // 다국어 텍스트 정규화
  private String normalizeMultilingualText(String text) {
    if (text == null) return "";

    return text.toLowerCase()
        .replaceAll("\\s+", " ")
        .replaceAll("[!@#$%^&*(),.?\":{}|<>]", " ")
        .trim();
  }

  // 다국어 콘텐츠 타입 매칭
  private double calculateMultilingualTypeMatch(String keyword, ContentType contentType) {
    keyword = keyword.toLowerCase();

    switch (contentType) {
      case MOVIE:
        if (keyword.contains("영화") || keyword.contains("movie") || keyword.contains("film") ||
            keyword.contains("시네마") || keyword.contains("cinema")) {
          return 1.0;
        }
        break;
      case TV:
        if (keyword.contains("드라마") || keyword.contains("drama") || keyword.contains("시리즈") ||
            keyword.contains("series") || keyword.contains("show")) {
          return 1.0;
        }
        break;
      case SPORTS:
        if (keyword.contains("스포츠") || keyword.contains("sports") || keyword.contains("축구") ||
            keyword.contains("야구") || keyword.contains("농구") || keyword.contains("경기") ||
            keyword.contains("football") || keyword.contains("basketball") || keyword.contains("game")) {
          return 1.0;
        }
        break;
    }

    return 0.0;
  }

  @Override
  public List<ContentDto> getRecommendationsByKeyword(UUID keywordId, UUID userId) {
    Keyword keyword = keywordRepository.findByIdAndUserId(keywordId, userId)
        .orElseThrow(KeywordNotFoundException::new);

    List<KeywordContent> keywordContents = keywordContentRepository.findByKeywordId(keywordId);

    return keywordContents.stream()
        .map(KeywordContent::getContent)
        .sorted((c1, c2) -> {
          double score1 = calculateAdvancedMatchingScore(keyword.getKeyword(), c1);
          double score2 = calculateAdvancedMatchingScore(keyword.getKeyword(), c2);
          return Double.compare(score2, score1);
        })
        .map(ContentDto::from)
        .toList();
  }

  @Override
  @Transactional
  public List<KeywordDto> getKeywordsByUser(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    List<Keyword> keywords = keywordRepository.findAllByUserId(userId);
    return keywords.stream().map(KeywordDto::from).toList();
  }

  @Override
  @Transactional
  public void batchCurationForNewContents(List<Content> newContents) {
    List<Keyword> allKeywords = keywordRepository.findAll();

    for (Content content : newContents) {
      for (Keyword keyword : allKeywords) {
        double score = calculateAdvancedMatchingScore(keyword.getKeyword(), content);

        if (score > 0.25) { // 임계값 조정
          boolean exists = keywordContentRepository.existsByKeywordIdAndContentId(
              keyword.getId(), content.getId());

          if (!exists) {
            KeywordContent keywordContent = new KeywordContent(keyword, content);
            keywordContentRepository.save(keywordContent);
          }
        }
      }
    }

    log.info("신규 콘텐츠 {}개에 대한 배치 큐레이션 완료", newContents.size());
  }

  @Override
  @Transactional
  public void updateContentRating(UUID contentId) {
    try {
      Content content = contentRepository.findById(contentId)
          .orElseThrow(ContentNotFoundException::new);

      BigDecimal avgRating = getAvgRating(content);
      content.setAvgRating(avgRating);
      contentRepository.save(content);

      log.info("콘텐츠 평점 업데이트: {} -> {}", content.getTitle(), content.getAvgRating());
    } catch (Exception e) {
      log.warn("평점 업데이트 실패: contentId={}, error={}", contentId, e.getMessage());
    }
  }

  private BigDecimal getAvgRating(Content content) {
    try {
      List<ReviewDto> reviews = reviewService.getAllByContent(content.getId());

      if (reviews.isEmpty()) {
        return null;
      }

      double averageRating = reviews.stream()
          .filter(review -> review.rating() != null)
          .mapToDouble(review -> review.rating().doubleValue())
          .average()
          .orElse(0.0);

      return BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);
    } catch (Exception e) {
      log.warn("평균 평점 계산 실패: contentId={}, error={}", content.getId(), e.getMessage());
      return null;
    }
  }

  @Override
  @Transactional
  public void delete(UUID keywordId, UUID userId) {
    Keyword keyword = keywordRepository.findById(keywordId)
        .orElseThrow(KeywordNotFoundException::new);

    if (!keyword.getUser().getId().equals(userId)) {
      throw new KeywordDeleteDeniedException();
    }

    keywordRepository.delete(keyword);
  }
}