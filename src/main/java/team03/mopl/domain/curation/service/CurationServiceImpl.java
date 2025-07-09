package team03.mopl.domain.curation.service;

import com.github.jfasttext.JFastText;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.entity.KeywordContent;
import team03.mopl.domain.curation.repository.KeywordContentRepository;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

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
  private JFastText koreanFastText;
  private JFastText englishFastText;

  // 언어별 패턴 정의
  private static final Pattern KOREAN_PATTERN = Pattern.compile("[가-힣]+");
  private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+");

  // 한국어 불용어
  private static final Set<String> KOREAN_STOPWORDS = Set.of(
      "이", "그", "저", "것", "들", "에", "는", "을", "를", "의", "가", "에서", "로", "으로",
      "와", "과", "하다", "되다", "있다", "없다", "같다", "다른", "새로운", "좋은", "나쁜"
  );

  // 영어 불용어
  private static final Set<String> ENGLISH_STOPWORDS = Set.of(
      "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
      "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did"
  );

  @PostConstruct
  @Override
  public void init() {
    initializeAI();
    initializeFastText();
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
      log.warn("Stanford NLP 초기화 실패: " + e.getMessage());
      this.nlpPipeline = null;
    }
  }

  private void initializeFastText() {
    try {
      koreanFastText = new JFastText();
      englishFastText = new JFastText();

      try {
        koreanFastText.loadModel("models/cc.ko.300.bin");
        log.info("initializeFastText - 한국어 FastText 모델 로드 완료");
      } catch (Exception e) {
        log.warn("한국어 FastText 모델 로드 실패, 기본 모드로 실행: {}", e.getMessage());
        koreanFastText = null;
      }

      try {
        englishFastText.loadModel("models/cc.en.300.bin");
        log.info("initializeFastText - 영어 FastText 모델 로드 완료");
      } catch (Exception e) {
        log.warn("영어 FastText 모델 로드 실패, 기본 모드로 실행: {}", e.getMessage());
        englishFastText = null;
      }

    } catch (Exception e) {
      log.warn("FastText 초기화 실패: " + e.getMessage());
      koreanFastText = null;
      englishFastText = null;
    }
  }


  // 사용자 키워드 등록
  @Override
  @Transactional
  public Keyword registerKeyword(UUID userId, String keywordText) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    // 다국어 키워드 정규화
    String normalizedKeyword = normalizeMultilingualText(keywordText);
    String language = detectLanguage(keywordText);

    Keyword keyword = Keyword.builder()
        .user(user)
        .keyword(normalizedKeyword)
        .build();
    keyword = keywordRepository.save(keyword);

    List<Content> recommendations = curateContentForKeyword(keyword);

    log.info("registerKeyword - 다국어 키워드 등록: '{}' -> '{}' [{}] (매칭된 콘텐츠: {}개)",
        keywordText, normalizedKeyword, language, recommendations.size());
    return keyword;
  }

  // AI 기반 키워드별 콘텐츠 큐레이션
  @Override
  @Transactional
  public List<Content> curateContentForKeyword(Keyword keyword) {
    String keywordText = keyword.getKeyword();
    List<Content> recommendations = new ArrayList<>();
    List<Content> allContents = contentRepository.findAll();

    // 각 콘텐츠에 대해 AI 점수 계산
    for (Content content : allContents) {
      double aiScore = calculateAIMatchingScore(keywordText, content);

      // 언어별 적응적 임계값
      double threshold = getLanguageBasedThreshold(keywordText);

      if (aiScore > threshold) {
        recommendations.add(content);

        KeywordContent keywordContent = new KeywordContent(keyword, content);
        keywordContentRepository.save(keywordContent);
      }
    }

    // 점수 순으로 정렬
    recommendations.sort((c1, c2) -> {
      double score1 = calculateAIMatchingScore(keywordText, c1);
      double score2 = calculateAIMatchingScore(keywordText, c2);
      return Double.compare(score2, score1);
    });

    return recommendations;
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
        return 0.25;
      case "english":
        return 0.3;
      case "mixed":
        return 0.28;
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

  // AI 매칭 점수 계산
  private double calculateAIMatchingScore(String keyword, Content content) {
    double totalScore = 0.0;

    // 1. 제목 매칭
    double titleScore = calculateTextSimilarity(keyword, content.getTitle()) * 0.4;

    // 2. 설명 매칭
    double descScore = calculateTextSimilarity(keyword, content.getDescription()) * 0.3;

    // 3. 장르/타입 매칭
    double typeScore = calculateMultilingualTypeMatch(keyword, content.getContentType()) * 0.2;

    // 4. 평점 보너스
    double ratingBonus = content.getAvgRating() != null ?
        content.getAvgRating().doubleValue() / 10.0 * 0.1 : 0.0;

    totalScore = titleScore + descScore + typeScore + ratingBonus;

    return Math.min(totalScore, 1.0);
  }

  // 텍스트 유사도 계산
  private double calculateTextSimilarity(String keyword, String text) {
    if (text == null || keyword == null) return 0.0;

    keyword = normalizeMultilingualText(keyword);
    text = normalizeMultilingualText(text);

    // 정확한 매칭
    if (text.contains(keyword)) return 1.0;

    double fastTextScore = calculateFastTextSimilarity(keyword, text);
    double ruleBasedScore = calculateHybridSimilarity(keyword, text);

    return fastTextScore * 0.7 + ruleBasedScore * 0.3;
  }

  // FastText 벡터 유사도 계산
  private double calculateFastTextSimilarity(String keyword, String text) {
    try {
      String keywordLang = detectLanguage(keyword);
      String textLang = detectLanguage(text);

      // 키워드와 텍스트를 토큰화
      List<String> keywordTokens = tokenizeForFastText(keyword, keywordLang);
      List<String> textTokens = tokenizeForFastText(text, textLang);

      if (keywordTokens.isEmpty() || textTokens.isEmpty()) {
        return 0.0;
      }

      // 각 토큰 쌍의 최대 유사도 계산
      double maxSimilarity = 0.0;
      int validComparisons = 0;

      for (String kToken : keywordTokens) {
        for (String tToken : textTokens) {
          double similarity = calculateWordVectorSimilarity(kToken, tToken, keywordLang, textLang);
          if (similarity > 0) {
            maxSimilarity = Math.max(maxSimilarity, similarity);
            validComparisons++;
          }
        }
      }

      // 평균 유사도와 최대 유사도의 가중평균
      if (validComparisons > 0) {
        double avgSimilarity = maxSimilarity * 0.8; // 최대값에 더 가중치
        return Math.min(avgSimilarity, 1.0);
      }

      return 0.0;

    } catch (Exception e) {
      log.warn("FastText 유사도 계산 실패: {}", e.getMessage());
      return 0.0;
    }
  }

  // FastText용 토큰화
  private List<String> tokenizeForFastText(String text, String language) {
    if (text == null || text.trim().isEmpty()) {
      return new ArrayList<>();
    }

    List<String> tokens = new ArrayList<>();
    String[] words = text.split("\\s+");

    for (String word : words) {
      word = word.toLowerCase().trim();
      if (word.length() > 1) {
        // 불용어 제거
        if ((language.equals("korean") && KOREAN_STOPWORDS.contains(word)) ||
            (language.equals("english") && ENGLISH_STOPWORDS.contains(word))) {
          continue;
        }

        tokens.add(word);
      }
    }
    return tokens;
  }

  // 단어 벡터 유사도 계산
  private double calculateWordVectorSimilarity(String word1, String word2, String lang1, String lang2) {
    try {
      // 같은 단어면 완전 일치
      if (word1.equals(word2)) return 1.0;

      // 부분 문자열 포함 관계
      if (word1.contains(word2) || word2.contains(word1)) return 0.8;

      List<Float> vector1 = null;
      List<Float> vector2 = null;

      // 언어별 JFastText 모델 사용
      if (lang1.equals("korean") && koreanFastText != null) {
        try {
          vector1 = koreanFastText.getVector(word1);  // getVector 메서드 사용
        } catch (Exception e) {
          // 단어가 없는 경우 null 유지
        }
      } else if (lang1.equals("english") && englishFastText != null) {
        try {
          vector1 = englishFastText.getVector(word1);  // getVector 메서드 사용
        } catch (Exception e) {
          // 단어가 없는 경우 null 유지
        }
      }

      if (lang2.equals("korean") && koreanFastText != null) {
        try {
          vector2 = koreanFastText.getVector(word2);  // getVector 메서드 사용
        } catch (Exception e) {
          // 단어가 없는 경우 null 유지
        }
      } else if (lang2.equals("english") && englishFastText != null) {
        try {
          vector2 = englishFastText.getVector(word2);  // getVector 메서드 사용
        } catch (Exception e) {
          // 단어가 없는 경우 null 유지
        }
      }

      // 벡터가 모두 있으면 코사인 유사도 계산
      if (vector1 != null && vector2 != null && !vector1.isEmpty() && !vector2.isEmpty()) {
        return calculateCosineSimilarityJFT(vector1, vector2);
      }

      // 벡터가 없으면 규칙 기반 유사도
      return calculateSemanticSimilarity(word1, word2);

    } catch (Exception e) {
      log.warn("JFastText 단어 벡터 유사도 계산 실패: {} vs {} - {}", word1, word2, e.getMessage());
      return 0.0;
    }
  }

  // JFastText List<Float> 코사인 유사도 계산
  private double calculateCosineSimilarityJFT(List<Float> vectorA, List<Float> vectorB) {
    try {
      if (vectorA.size() != vectorB.size()) return 0.0;

      double dotProduct = 0.0;
      double normA = 0.0;
      double normB = 0.0;

      for (int i = 0; i < vectorA.size(); i++) {
        float a = vectorA.get(i);
        float b = vectorB.get(i);
        dotProduct += a * b;
        normA += a * a;
        normB += b * b;
      }

      double denominator = Math.sqrt(normA) * Math.sqrt(normB);
      return denominator == 0.0 ? 0.0 : dotProduct / denominator;

    } catch (Exception e) {
      log.warn("코사인 유사도 계산 실패: {}", e.getMessage());
      return 0.0;
    }
  }

  // 간단한 백업 유사도 (FastText 실패시에만 사용)
  private double calculateSemanticSimilarity(String word1, String word2) {
    // 한영 번역 매칭만 유지 (가장 확실한 관계)
    Map<String, String> translationMap = createKoreanEnglishTranslationMap();
    if (translationMap.containsKey(word1) && translationMap.get(word1).equals(word2) ||
        translationMap.containsKey(word2) && translationMap.get(word2).equals(word1)) {
      return 0.8; // 번역 관계 유사도
    }

    return 0.0;
  }

  // 하이브리드 유사도 계산 (기존 규칙 기반)
  private double calculateHybridSimilarity(String keyword, String text) {
    List<String> keywordTokens = tokenizeMultilingual(keyword);
    List<String> textTokens = tokenizeMultilingual(text);

    if (keywordTokens.isEmpty()) return 0.0;

    double directMatch = calculateDirectMatch(keywordTokens, textTokens);
    double semanticMatch = calculateHybridSemanticMatch(keywordTokens, textTokens);

    return directMatch * 0.7 + semanticMatch * 0.3;
  }

  // 다국어 토큰화
  private List<String> tokenizeMultilingual(String text) {
    if (text == null || text.trim().isEmpty()) {
      return new ArrayList<>();
    }

    List<String> tokens = new ArrayList<>();
    String[] words = text.split("\\s+");

    for (String word : words) {
      if (word.length() > 1) {
        String lang = detectLanguage(word);

        // 불용어 제거
        if ((lang.equals("korean") && KOREAN_STOPWORDS.contains(word)) ||
            (lang.equals("english") && ENGLISH_STOPWORDS.contains(word))) {
          continue;
        }

        tokens.add(word);

        // 한국어인 경우 복합어 처리
        if (lang.equals("korean") && word.length() >= 3) {
          for (int i = 0; i <= word.length() - 2; i++) {
            String subword = word.substring(i, i + 2);
            if (!KOREAN_STOPWORDS.contains(subword)) {
              tokens.add(subword);
            }
          }
        }
      }
    }
    return tokens.stream().distinct().collect(Collectors.toList());
  }

  // 직접 매칭 계산
  private double calculateDirectMatch(List<String> keywordTokens, List<String> textTokens) {
    if (keywordTokens.isEmpty()) return 0.0;

    int matches = 0;
    for (String kToken : keywordTokens) {
      if (textTokens.contains(kToken)) {
        matches++;
      }
    }

    return (double) matches / keywordTokens.size();
  }

  // 하이브리드 의미 매칭 (간소화)
  private double calculateHybridSemanticMatch(List<String> keywordTokens, List<String> textTokens) {
    Map<String, String> translationMap = createKoreanEnglishTranslationMap();

    int semanticMatches = 0;
    int totalComparisons = 0;

    for (String kToken : keywordTokens) {
      for (String tToken : textTokens) {
        totalComparisons++;

        // 직접 매칭
        if (kToken.equals(tToken)) {
          semanticMatches++;
          continue;
        }

        // 번역 매칭만 유지 (가장 확실한 관계)
        if (translationMap.containsKey(kToken) && translationMap.get(kToken).equals(tToken) ||
            translationMap.containsKey(tToken) && translationMap.get(tToken).equals(kToken)) {
          semanticMatches++;
        }
      }
    }

    return totalComparisons > 0 ? (double) semanticMatches / totalComparisons : 0.0;
  }

  // 한영 번역 매핑 (핵심 관계만 유지)
  private Map<String, String> createKoreanEnglishTranslationMap() {
    Map<String, String> map = new HashMap<>();

    // 가장 확실한 번역 관계만 유지
    map.put("액션", "action");
    map.put("드라마", "drama");
    map.put("코미디", "comedy");
    map.put("로맨스", "romance");
    map.put("공포", "horror");
    map.put("영화", "movie");
    map.put("축구", "football");
    map.put("농구", "basketball");
    map.put("야구", "baseball");
    map.put("스포츠", "sports");
    map.put("한국", "korean");
    map.put("미국", "american");

    return map;
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

  // TODO: 커서 페이지네이션
  @Override
  public List<Content> getRecommendationsByKeyword(UUID keywordId, UUID userId) {
    Keyword keyword = keywordRepository.findByIdAndUserId(keywordId, userId)
        .orElseThrow(KeywordNotFoundException::new);

    List<KeywordContent> keywordContents = keywordContentRepository.findByKeywordId(keywordId);

    return keywordContents.stream()
        .map(KeywordContent::getContent)
        .sorted((c1, c2) -> {
          double score1 = calculateAIMatchingScore(keyword.getKeyword(), c1);
          double score2 = calculateAIMatchingScore(keyword.getKeyword(), c2);
          return Double.compare(score2, score1); // 높은 점수부터
        })
        .collect(Collectors.toList());
  }

  // TODO: 배치작업
  // 배치 큐레이션 (새 콘텐츠에 대해 기존 키워드 매칭)
  @Override
  @Transactional
  public void batchCurationForNewContents(List<Content> newContents) {
    List<Keyword> allKeywords = keywordRepository.findAll();

    for (Content content : newContents) {
      for (Keyword keyword : allKeywords) {
        double score = calculateAIMatchingScore(keyword.getKeyword(), content);

        if (score > 0.3) {
          boolean exists = keywordContentRepository.existsByKeywordIdAndContentId(
              keyword.getId(), content.getId());

          if (!exists) {
            KeywordContent keywordContent = new KeywordContent(keyword, content);
            keywordContentRepository.save(keywordContent);
          }
        }
      }
    }

    log.info("batchCurationForNewContents - 신규 콘텐츠 {}개에 대한 배치 큐레이션 완료", newContents.size());
  }

  // TODO: review 새로 작성되면 자동으로 평점 업데이트 하는 이벤트 생성
  @Override
  @Transactional
  public void updateContentRating(UUID contentId) {
    try {
      Content content = contentRepository.findById(contentId)
          .orElseThrow(ContentNotFoundException::new);

      BigDecimal avgRating = getAvgRating(content);

      content.setAvgRating(avgRating);
      contentRepository.save(content);

      log.info("updateContentRating - 콘텐츠 평점 업데이트: {} -> {}", content.getTitle(), content.getAvgRating());

    } catch (Exception e) {
      log.warn("평점 업데이트 실패: contentId={}, error={}", contentId, e.getMessage());
    }
  }

  // TODO: 리뷰 등록되면 자동으로 평균 평점 계산하도록 event 생성
  private BigDecimal getAvgRating(Content content) {
    try {
      List<ReviewDto> reviews = reviewService.getAllByContent(content.getId());

      if (reviews.isEmpty()) {
        log.info("getAvgRating - 콘텐츠 {}에 대한 리뷰가 없습니다.", content.getId());
        return null;
      }

      double averageRating = reviews.stream()
          .filter(review -> review.rating() != null)
          .mapToDouble(review -> review.rating().doubleValue())
          .average()
          .orElse(0.0);

      log.info("getAvgRating - 콘텐츠 {} 평점 계산: 총 {}개 리뷰, 평균 {}",
          content.getId(), reviews.size(), averageRating);

      // 소수점 둘째 자리까지 반올림
      return BigDecimal.valueOf(averageRating)
          .setScale(2, RoundingMode.HALF_UP);

    } catch (Exception e) {
      log.warn("평균 평점 계산 실패: contentId={}, error={}", content.getId(), e.getMessage());
      return null;
    }
  }

  @Override
  @Transactional
  public void delete(UUID keywordId) {
    Keyword keyword = keywordRepository.findById(keywordId)
            .orElseThrow(KeywordNotFoundException::new);

    keywordRepository.delete(keyword);
  }
}