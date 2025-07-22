package team03.mopl.domain.curation.service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.curation.ContentRatingUpdateException;
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

  // 최적화된 배치 처리 설정
  private static final int OPTIMIZED_BATCH_SIZE = 2000;
  private static final int BULK_INSERT_SIZE = 500;
  private static final int MAX_RESULTS_PER_KEYWORD = 5000;

  // NLP 컴포넌트들 (재사용)
  private StanfordCoreNLP nlpPipeline;
  private Komoran komoran;

  // 언어별 패턴 정의
  private static final Pattern KOREAN_PATTERN = Pattern.compile("[가-힣]+");
  private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+");

  // 한국어 불용어
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

  // 한국어 동의어 사전
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

  // 한영 번역 사전
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
    initializeNLPComponents();
    log.info("최적화된 큐레이션 서비스 초기화 완료");
  }

  private void initializeNLPComponents() {
    // NLP 컴포넌트 병렬 초기화
    CompletableFuture<Void> stanfordInit = CompletableFuture.runAsync(() -> {
      try {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
        props.setProperty("ner.useSUTime", "false");
        props.setProperty("threads", "4");
        this.nlpPipeline = new StanfordCoreNLP(props);
        log.info("Stanford CoreNLP 초기화 완료");
      } catch (Exception e) {
        log.warn("Stanford NLP 초기화 실패: {}", e.getMessage());
        this.nlpPipeline = null;
      }
    });

    CompletableFuture<Void> komoranInit = CompletableFuture.runAsync(() -> {
      try {
        this.komoran = new Komoran(DEFAULT_MODEL.FULL);
        log.info("Komoran 형태소 분석기 초기화 완료");
      } catch (Exception e) {
        log.warn("Komoran 초기화 실패: {}", e.getMessage());
        this.komoran = null;
      }
    });

    CompletableFuture.allOf(stanfordInit, komoranInit).join();
  }

  @Override
  @Transactional
  public KeywordDto registerKeyword(UUID userId, String keywordText) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    String normalizedKeyword = normalizeMultilingualText(keywordText);

    Keyword keyword = Keyword.builder()
        .user(user)
        .keyword(normalizedKeyword)
        .build();
    keyword = keywordRepository.save(keyword);

    calculateScoresAsyncOptimized(keyword);

    log.info("키워드 등록 완료: '{}' -> '{}', ID: {}",
        keywordText, normalizedKeyword, keyword.getId());

    return KeywordDto.from(keyword);
  }

  @Async("scoreCalculationExecutor")
  @Transactional
  public CompletableFuture<Void> calculateScoresAsyncOptimized(Keyword keyword) {
    try {
      log.info("최적화된 점수 계산 시작 - 키워드: '{}'", keyword.getKeyword());

      keywordContentRepository.deleteByKeywordId(keyword.getId());

      // 키워드 전처리 (한 번만 수행)
      String normalizedKeyword = normalizeMultilingualText(keyword.getKeyword());
      List<String> keywordTokens = extractOptimizedTokens(keyword.getKeyword());
      double threshold = getLanguageBasedThreshold(keyword.getKeyword());

      AtomicInteger processedCount = new AtomicInteger(0);
      AtomicInteger savedCount = new AtomicInteger(0);

      // Phase 1: titleNormalized 필드로 빠른 사전 필터링
      List<Content> fastMatchedContents = contentRepository.findByTitleNormalizedContaining(normalizedKeyword);

      if (!fastMatchedContents.isEmpty()) {
        log.info("Phase 1: titleNormalized 사전 필터링 완료 - {} 건 발견", fastMatchedContents.size());

        List<KeywordContent> phase1Results = fastMatchedContents.parallelStream()
            .map(content -> {
              processedCount.incrementAndGet();
              double score = calculateFastMatchingScore(normalizedKeyword, keywordTokens, content);
              return KeywordContent.builder()
                  .keyword(keyword)
                  .content(content)
                  .score(Math.max(score, 0.6))
                  .build();
            })
            .collect(Collectors.toList());

        keywordContentRepository.saveAll(phase1Results);
        savedCount.addAndGet(phase1Results.size());
        log.info("Phase 1 저장 완료: {} 건", phase1Results.size());
      }

      // Phase 2: 나머지 콘텐츠들 중에서 추가 검색
      if (savedCount.get() < MAX_RESULTS_PER_KEYWORD) {
        log.info("Phase 2: 추가 콘텐츠 검색 시작...");

        Set<UUID> processedIds = fastMatchedContents.stream()
            .map(Content::getId)
            .collect(Collectors.toSet());

        // 페이징 방식으로 처리 (더 안전)
        int page = 0;
        int pageSize = 1000;
        Pageable pageable = PageRequest.of(page, pageSize);

        while (true) {
          Page<Content> contentPage = contentRepository.findAllContents(pageable);

          if (contentPage.isEmpty()) break;

          List<KeywordContent> batchResults = contentPage.getContent()
              .parallelStream()
              .filter(content -> !processedIds.contains(content.getId()))
              .map(content -> {
                processedCount.incrementAndGet();
                return calculateKeywordContentScore(keyword, normalizedKeyword, keywordTokens, content);
              })
              .filter(kc -> kc.getScore() > threshold)
              .collect(Collectors.toList());

          if (!batchResults.isEmpty()) {
            keywordContentRepository.saveAll(batchResults);
            savedCount.addAndGet(batchResults.size());
          }

          // 충분한 결과가 모이면 중단
          if (savedCount.get() >= MAX_RESULTS_PER_KEYWORD) break;

          // 다음 페이지로
          pageable = contentPage.nextPageable();
          if (!contentPage.hasNext()) break;
        }
      }

      log.info("점수 계산 완료 - 키워드: '{}', 처리: {}, 저장: {}",
          keyword.getKeyword(), processedCount.get(), savedCount.get());

      return CompletableFuture.completedFuture(null);

    } catch (Exception e) {
      log.error("점수 계산 실패 - 키워드: '{}'", keyword.getKeyword(), e);
      return CompletableFuture.failedFuture(e);
    }
  }

  private KeywordContent calculateKeywordContentScore(
      Keyword keyword,
      String normalizedKeyword,
      List<String> keywordTokens,
      Content content) {

    double score = calculateFastMatchingScore(normalizedKeyword, keywordTokens, content);

    return KeywordContent.builder()
        .keyword(keyword)
        .content(content)
        .score(score)
        .build();
  }

  // 점수 분포 개선을 위한 가중치 조정된 매칭 점수 계산
  private double calculateFastMatchingScore(
      String normalizedKeyword,
      List<String> keywordTokens,
      Content content) {

    if (content.getTitleNormalized() == null && content.getDescription() == null) {
      return 0.0;
    }

    double totalScore = 0.0;
    double titleScore = 0.0;
    double descriptionScore = 0.0;

    // === 제목 매칭 (가중치 대폭 강화) ===
    String nameNormalized = content.getTitleNormalized();
    if (nameNormalized != null) {
      // 1. 완전 일치 - 매우 높은 점수
      if (nameNormalized.contains(normalizedKeyword)) {
        if (nameNormalized.equals(normalizedKeyword)) {
          titleScore = 1.0; // 완전 동일
        } else if (nameNormalized.startsWith(normalizedKeyword) || nameNormalized.endsWith(normalizedKeyword)) {
          titleScore = 0.9; // 시작/끝 일치
        } else {
          titleScore = 0.8; // 부분 일치
        }
      } else {
        // 2. 토큰 기반 유사도 - 세분화된 점수
        String originalTitle = content.getTitle();
        if (originalTitle != null) {
          List<String> titleTokens = extractOptimizedTokens(originalTitle);
          double jaccardScore = calculateJaccardSimilarity(keywordTokens, titleTokens);

          if (jaccardScore > 0.7) titleScore = 0.7;
          else if (jaccardScore > 0.5) titleScore = 0.6;
          else if (jaccardScore > 0.3) titleScore = 0.4;
          else if (jaccardScore > 0.1) titleScore = 0.2;
          else titleScore = jaccardScore * 0.1;
        }
      }
    }

    // === 설명 매칭 (차등 점수 적용) ===
    String description = content.getDescription();
    if (description != null) {
      String normalizedDesc = normalizeMultilingualText(description);

      // 완전 일치
      if (normalizedDesc.contains(normalizedKeyword)) {
        int keywordLength = normalizedKeyword.length();
        int descLength = normalizedDesc.length();

        // 키워드가 설명에서 차지하는 비중에 따라 점수 차등
        double ratio = (double) keywordLength / descLength;
        if (ratio > 0.5) descriptionScore = 0.5;      // 키워드가 설명의 절반 이상
        else if (ratio > 0.2) descriptionScore = 0.4; // 키워드가 설명의 20% 이상
        else if (ratio > 0.1) descriptionScore = 0.3; // 키워드가 설명의 10% 이상
        else descriptionScore = 0.2;                  // 그 외
      } else {
        // 부분 매칭
        double wordMatch = calculateSimpleWordMatch(normalizedKeyword, normalizedDesc);
        if (wordMatch > 0.8) descriptionScore = 0.3;
        else if (wordMatch > 0.5) descriptionScore = 0.2;
        else if (wordMatch > 0.2) descriptionScore = 0.1;
        else descriptionScore = wordMatch * 0.05;
      }
    }

    // === 가중치 적용 (제목에 더 큰 가중치) ===
    totalScore = titleScore * 0.7 + descriptionScore * 0.2;

    // === 콘텐츠 타입 매칭 강화 ===
    double typeScore = getEnhancedTypeMatchScore(normalizedKeyword, content.getContentType());
    totalScore += typeScore * 0.05;

    // === 평점 보너스 개선 (비선형 점수) ===
    if (content.getAvgRating() != null) {
      double rating = content.getAvgRating().doubleValue();
      double ratingBonus = 0.0;

      if (rating >= 9.0) ratingBonus = 0.05;      // 9점 이상
      else if (rating >= 8.0) ratingBonus = 0.03; // 8점 이상
      else if (rating >= 7.0) ratingBonus = 0.02; // 7점 이상
      else if (rating >= 6.0) ratingBonus = 0.01; // 6점 이상

      totalScore += ratingBonus;
    }

    // === 동의어 및 번역어 보너스 추가 ===
    double synonymBonus = calculateSynonymBonus(normalizedKeyword, content);
    totalScore += synonymBonus * 0.03;

    return Math.min(totalScore, 1.0);
  }

  private double getEnhancedTypeMatchScore(String keyword, ContentType contentType) {
    switch (contentType) {
      case MOVIE:
        if (keyword.equals("영화") || keyword.equals("movie")) return 1.0;
        if (keyword.contains("영화") || keyword.contains("movie") ||
            keyword.contains("film") || keyword.contains("시네마")) return 0.8;
        break;
      case TV:
        if (keyword.equals("드라마") || keyword.equals("drama")) return 1.0;
        if (keyword.contains("드라마") || keyword.contains("drama") ||
            keyword.contains("시리즈") || keyword.contains("series") ||
            keyword.contains("show") || keyword.contains("방송")) return 0.8;
        break;
      case SPORTS:
        if (keyword.equals("스포츠") || keyword.equals("sports")) return 1.0;
        if (keyword.contains("스포츠") || keyword.contains("sports") ||
            keyword.contains("축구") || keyword.contains("야구") ||
            keyword.contains("농구") || keyword.contains("경기") ||
            keyword.contains("football") || keyword.contains("basketball") ||
            keyword.contains("baseball") || keyword.contains("game")) return 0.8;
        break;
    }
    return 0.0;
  }

  private double calculateSynonymBonus(String keyword, Content content) {
    if (content.getTitleNormalized() == null) return 0.0;

    String contentTitle = content.getTitleNormalized();

    // 동의어 매칭 체크
    for (Map.Entry<String, Set<String>> entry : SYNONYM_MAP.entrySet()) {
      String mainWord = entry.getKey();
      Set<String> synonyms = entry.getValue();

      if (keyword.contains(mainWord) &&
          synonyms.stream().anyMatch(contentTitle::contains)) {
        return 1.0;
      }

      if (synonyms.contains(keyword) && contentTitle.contains(mainWord)) {
        return 1.0;
      }
    }

    // 번역어 매칭 체크
    for (Map.Entry<String, String> entry : TRANSLATION_MAP.entrySet()) {
      String korean = entry.getKey();
      String english = entry.getValue();

      if (keyword.contains(korean) && contentTitle.contains(english)) {
        return 1.0;
      }
      if (keyword.contains(english) && contentTitle.contains(korean)) {
        return 1.0;
      }
    }

    return 0.0;
  }

  private List<String> extractOptimizedTokens(String text) {
    if (text == null || text.trim().isEmpty()) {
      return Collections.emptyList();
    }

    List<String> tokens = new ArrayList<>();

    // 언어별 최적화된 처리
    if (KOREAN_PATTERN.matcher(text).find()) {
      // 한국어: 형태소 분석 (필요시에만)
      if (text.length() <= 20) { // 짧은 텍스트만 형태소 분석
        tokens.addAll(extractKoreanMorphemes(text));
      } else {
        // 긴 텍스트는 단순 분할
        tokens.addAll(Arrays.asList(text.split("\\s+")));
      }
    }

    if (ENGLISH_PATTERN.matcher(text).find()) {
      // 영어: 간단한 토큰화
      tokens.addAll(Arrays.stream(text.toLowerCase().split("\\W+"))
          .filter(word -> word.length() > 1)
          .filter(word -> !ENGLISH_STOPWORDS.contains(word))
          .collect(Collectors.toList()));
    }

    return tokens.stream()
        .distinct()
        .filter(t -> t.length() > 1)
        .collect(Collectors.toList());
  }

  private List<String> extractKoreanMorphemes(String text) {
    if (komoran == null) {
      return Arrays.asList(text.split("\\s+"));
    }

    try {
      KomoranResult result = komoran.analyze(text);
      return result.getMorphesByTags("NNG", "NNP", "SL") // 핵심 품사만
          .stream()
          .filter(morph -> morph.length() > 1)
          .filter(morph -> !KOREAN_STOPWORDS.contains(morph))
          .distinct()
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.debug("형태소 분석 실패, 단순 분할 사용: {}", e.getMessage());
      return Arrays.asList(text.split("\\s+"));
    }
  }

  private double calculateJaccardSimilarity(List<String> tokens1, List<String> tokens2) {
    if (tokens1.isEmpty() || tokens2.isEmpty()) return 0.0;

    Set<String> set1 = new HashSet<>(tokens1);
    Set<String> set2 = new HashSet<>(tokens2);

    // 교집합 크기 계산
    int intersectionSize = (int) set1.stream()
        .filter(set2::contains)
        .count();

    if (intersectionSize == 0) return 0.0;

    // 합집합 크기 = set1.size() + set2.size() - intersection
    int unionSize = set1.size() + set2.size() - intersectionSize;

    return (double) intersectionSize / unionSize;
  }

  private double calculateSimpleWordMatch(String keyword, String text) {
    String[] keywordWords = keyword.split("\\s+");
    String[] textWords = text.split("\\s+");

    int matches = 0;
    for (String kWord : keywordWords) {
      for (String tWord : textWords) {
        if (kWord.equals(tWord) ||
            (kWord.length() > 2 && tWord.contains(kWord))) {
          matches++;
          break;
        }
      }
    }

    return keywordWords.length > 0 ? (double) matches / keywordWords.length : 0.0;
  }

  @Override
  @Transactional
  public void batchCurationForNewContents(List<Content> newContents) {
    if (newContents.isEmpty()) return;

    List<Keyword> allKeywords = keywordRepository.findAll();
    if (allKeywords.isEmpty()) return;

    AtomicInteger totalSaved = new AtomicInteger(0);

    // 키워드별 병렬 처리
    allKeywords.parallelStream()
        .forEach(keyword -> {
          String normalizedKeyword = normalizeMultilingualText(keyword.getKeyword());
          List<String> keywordTokens = extractOptimizedTokens(keyword.getKeyword());
          double threshold = getLanguageBasedThreshold(keyword.getKeyword());

          List<KeywordContent> newKeywordContents = newContents.stream()
              .map(content -> calculateKeywordContentScore(keyword, normalizedKeyword, keywordTokens, content))
              .filter(kc -> kc.getScore() > threshold)
              .collect(Collectors.toList());

          if (!newKeywordContents.isEmpty()) {
            keywordContentRepository.saveAll(newKeywordContents);
            totalSaved.addAndGet(newKeywordContents.size());
          }
        });

    log.info("신규 콘텐츠 {} 건 배치 처리 완료, 총 저장: {} 건",
        newContents.size(), totalSaved.get());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ContentDto> getRecommendationsByKeyword(UUID keywordId, UUID userId) {
    Keyword keyword = keywordRepository.findByIdAndUserId(keywordId, userId)
        .orElseThrow(KeywordNotFoundException::new);

    // 우선 titleNormalized 필드로 빠른 필터링 시도
    String normalizedKeyword = normalizeMultilingualText(keyword.getKeyword());
    List<Content> fastMatchContents = contentRepository.findByTitleNormalizedContaining(normalizedKeyword);

    // 빠른 매칭으로 충분한 결과가 있으면 사용
    if (!fastMatchContents.isEmpty() && fastMatchContents.size() >= 10) {
      log.info("titleNormalized 필드로 빠른 검색 완료: {} 건", fastMatchContents.size());

      // 점수 0.5 이상인 콘텐츠만 필터링하여 반환
      return fastMatchContents.stream()
          .map(content -> {
            double score = calculateFastMatchingScore(normalizedKeyword,
                extractOptimizedTokens(keyword.getKeyword()), content);
            return Map.entry(ContentDto.from(content), score);
          })
          .filter(entry -> entry.getValue() >= 0.5) // 점수 0.5 이상만
          .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 점수 내림차순
          .map(Map.Entry::getKey) // ContentDto만 추출
          .collect(Collectors.toList());
    }

    // 기존 방식 fallback - 점수 0.5 이상만 필터링
    if (!keywordContentRepository.existsByKeywordId(keywordId)) {
      return handleMissingScores(keyword);
    }

    List<KeywordContent> highScoreContents = keywordContentRepository
        .findByKeywordIdAndScoreGreaterThanEqualOrderByScoreDesc(keywordId, 0.5);

    return highScoreContents.stream()
        .map(kc -> ContentDto.from(kc.getContent()))
        .collect(Collectors.toList());
  }



  private List<ContentDto> handleMissingScores(Keyword keyword) {
    log.warn("점수 계산이 완료되지 않음 - 키워드 ID: {}", keyword.getId());
    return List.of();
  }

  private String normalizeMultilingualText(String text) {
    if (text == null) return "";
    return text.toLowerCase()
        .replaceAll("\\s+", " ")
        .replaceAll("[!@#$%^&*(),.?\":{}|<>]", " ")
        .trim();
  }

  private double getLanguageBasedThreshold(String text) {
    String language = detectLanguage(text);

    switch (language) {
      case "korean": return 0.05;
      case "english": return 0.08;
      case "mixed": return 0.06;
      default: return 0.1;
    }
  }

  private String detectLanguage(String text) {
    if (text == null || text.trim().isEmpty()) return "unknown";

    boolean hasKorean = KOREAN_PATTERN.matcher(text).find();
    boolean hasEnglish = ENGLISH_PATTERN.matcher(text).find();

    if (hasKorean && hasEnglish) return "mixed";
    else if (hasKorean) return "korean";
    else if (hasEnglish) return "english";
    else return "other";
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
  public void updateContentRating(UUID contentId) {
    try {
      Content content = contentRepository.findById(contentId)
          .orElseThrow(ContentNotFoundException::new);

      BigDecimal avgRating = getAvgRating(content);
      content.setAvgRating(avgRating);
      contentRepository.save(content);

      log.info("updateContentRating - 콘텐츠 평점 업데이트 완료: {} -> {}", content.getTitle(), avgRating);
    } catch (ContentNotFoundException e) {
      log.warn("콘텐츠를 찾을 수 없음: {}", contentId);
      throw e;
    } catch (Exception e) {
      log.warn("평점 업데이트 실패: contentId={}", contentId, e);
      throw new ContentRatingUpdateException();
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
      throw e;
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
