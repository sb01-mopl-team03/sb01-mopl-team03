//package team03.mopl.domain.curation.service;
//
//import edu.stanford.nlp.ling.CoreAnnotations;
//import edu.stanford.nlp.ling.CoreLabel;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.pipeline.StanfordCoreNLP;
//import edu.stanford.nlp.util.CoreMap;
//import jakarta.annotation.PostConstruct;
//import java.util.concurrent.CompletableFuture;
//import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
//import kr.co.shineware.nlp.komoran.core.Komoran;
//import kr.co.shineware.nlp.komoran.model.KomoranResult;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import team03.mopl.common.exception.content.ContentNotFoundException;
//import team03.mopl.common.exception.curation.ContentRatingUpdateException;
//import team03.mopl.common.exception.curation.KeywordDeleteDeniedException;
//import team03.mopl.common.exception.curation.KeywordNotFoundException;
//import team03.mopl.common.exception.user.UserNotFoundException;
//import team03.mopl.domain.content.Content;
//import team03.mopl.domain.content.ContentType;
//import team03.mopl.domain.content.dto.ContentDto;
//import team03.mopl.domain.content.repository.ContentRepository;
//import team03.mopl.domain.curation.dto.KeywordDto;
//import team03.mopl.domain.curation.entity.Keyword;
//import team03.mopl.domain.curation.entity.KeywordContent;
//import team03.mopl.domain.curation.repository.KeywordContentRepository;
//import team03.mopl.domain.curation.repository.KeywordRepository;
//import team03.mopl.domain.review.dto.ReviewDto;
//import team03.mopl.domain.review.service.ReviewService;
//import team03.mopl.domain.user.User;
//import team03.mopl.domain.user.UserRepository;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.*;
//import java.util.regex.Pattern;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class CurationServiceImpl implements CurationService {
//  private final ReviewService reviewService;
//  private final ContentRepository contentRepository;
//  private final KeywordRepository keywordRepository;
//  private final KeywordContentRepository keywordContentRepository;
//  private final UserRepository userRepository;
//
//  private StanfordCoreNLP nlpPipeline;
//  private Komoran komoran;
//
//  // 언어별 패턴 정의
//  private static final Pattern KOREAN_PATTERN = Pattern.compile("[가-힣]+");
//  private static final Pattern ENGLISH_PATTERN = Pattern.compile("[a-zA-Z]+");
//
//  // 한국어 불용어 (확장)
//  private static final Set<String> KOREAN_STOPWORDS = Set.of(
//      "이", "그", "저", "것", "들", "에", "는", "을", "를", "의", "가", "에서", "로", "으로",
//      "와", "과", "하다", "되다", "있다", "없다", "같다", "다른", "새로운", "좋은", "나쁜",
//      "이런", "저런", "그런", "어떤", "무엇", "누구", "언제", "어디서", "왜", "어떻게"
//  );
//
//  // 영어 불용어
//  private static final Set<String> ENGLISH_STOPWORDS = Set.of(
//      "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with",
//      "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did"
//  );
//
//  // 한국어 동의어 사전 (확장 가능)
//  private static final Map<String, Set<String>> SYNONYM_MAP = Map.of(
//      "영화", Set.of("무비", "시네마", "필름", "movie", "film"),
//      "드라마", Set.of("연속극", "시리즈", "TV드라마", "drama", "series"),
//      "음악", Set.of("뮤직", "노래", "곡", "music", "song"),
//      "스포츠", Set.of("운동", "체육", "경기", "sports", "game"),
//      "게임", Set.of("놀이", "경기", "게이밍", "game", "gaming"),
//      "코미디", Set.of("개그", "웃긴", "유머", "comedy", "funny"),
//      "액션", Set.of("액숀", "격투", "전투", "action", "fight"),
//      "로맨스", Set.of("사랑", "연애", "romance", "love"),
//      "공포", Set.of("호러", "무서운", "horror", "scary"),
//      "판타지", Set.of("환상", "마법", "fantasy", "magic")
//  );
//
//  // 한영 번역 사전 (확장)
//  private static final Map<String, String> TRANSLATION_MAP = Map.ofEntries(
//      Map.entry("액션", "action"),
//      Map.entry("드라마", "drama"),
//      Map.entry("코미디", "comedy"),
//      Map.entry("로맨스", "romance"),
//      Map.entry("공포", "horror"),
//      Map.entry("스릴러", "thriller"),
//      Map.entry("판타지", "fantasy"),
//      Map.entry("음악", "music"),
//      Map.entry("영화", "movie"),
//      Map.entry("스포츠", "sports"),
//      Map.entry("축구", "football"),
//      Map.entry("농구", "basketball"),
//      Map.entry("야구", "baseball"),
//      Map.entry("한국", "korean"),
//      Map.entry("미국", "american"),
//      Map.entry("일본", "japanese"),
//      Map.entry("중국", "chinese")
//  );
//
//  @PostConstruct
//  @Override
//  public void init() {
//    initializeAI();
//    initializeKomoran();
//    log.info("init - 큐레이션 서비스 초기화 완료 (Komoran + 혼합 언어 분석 모드)");
//  }
//
//  private void initializeAI() {
//    try {
//      Properties props = new Properties();
//      props.setProperty("annotators", "tokenize,ssplit,pos,lemma");
//      props.setProperty("ner.useSUTime", "false");
//      this.nlpPipeline = new StanfordCoreNLP(props);
//      log.info("initializeAI - Stanford CoreNLP 초기화 완료");
//    } catch (Exception e) {
//      log.warn("Stanford NLP 초기화 실패: {}", e.getMessage());
//      this.nlpPipeline = null;
//    }
//  }
//
//  // 형태소 기반 유사도 계산 (혼합 언어 대응)
//  private double calculateMorphemeSimilarity(String keyword, String text) {
//    try {
//      List<String> keywordTokens = extractMultilingualTokens(keyword);
//      List<String> textTokens = extractMultilingualTokens(text);
//
//      if (keywordTokens.isEmpty() || textTokens.isEmpty()) return 0.0;
//
//      Set<String> keywordSet = new HashSet<>(keywordTokens);
//      Set<String> textSet = new HashSet<>(textTokens);
//
//      Set<String> intersection = new HashSet<>(keywordSet);
//      intersection.retainAll(textSet);
//
//      Set<String> union = new HashSet<>(keywordSet);
//      union.addAll(textSet);
//
//      return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
//
//    } catch (Exception e) {
//      log.warn("혼합 언어 유사도 계산 실패: {}", e.getMessage());
//      return 0.0;
//    }
//  }
//
//  private List<String> extractMultilingualTokens(String text) {
//    List<String> tokens = new ArrayList<>();
//
//    if (text == null || text.isBlank()) return tokens;
//
//    if (KOREAN_PATTERN.matcher(text).find()) {
//      tokens.addAll(extractMeaningfulMorphemes(text));
//    }
//
//    if (ENGLISH_PATTERN.matcher(text).find()) {
//      tokens.addAll(lemmatizeText(text));
//    }
//
//    return tokens.stream()
//        .map(String::toLowerCase)
//        .filter(t -> t.length() > 1)
//        .distinct()
//        .toList();
//  }
//
//  private List<String> lemmatizeText(String text) {
//    if (nlpPipeline == null || text == null || text.trim().isEmpty()) return List.of();
//
//    try {
//      Annotation document = new Annotation(text);
//      nlpPipeline.annotate(document);
//
//      List<String> lemmas = new ArrayList<>();
//      List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
//
//      for (CoreMap sentence : sentences) {
//        for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//          String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);
//          if (lemma != null && lemma.length() > 1 && !ENGLISH_STOPWORDS.contains(lemma.toLowerCase())) {
//            lemmas.add(lemma.toLowerCase());
//          }
//        }
//      }
//
//      return lemmas.stream().distinct().toList();
//    } catch (Exception e) {
//      log.warn("Lemmatization 실패: {}", e.getMessage());
//      return List.of();
//    }
//  }
//
//  // Komoran 형태소 분석기 초기화
//  private void initializeKomoran() {
//    try {
//      this.komoran = new Komoran(DEFAULT_MODEL.FULL);
//      log.info("initializeKomoran - Komoran 형태소 분석기 초기화 완료");
//    } catch (Exception e) {
//      log.warn("Komoran 초기화 실패: {}", e.getMessage());
//      this.komoran = null;
//    }
//  }
//
//  // 키워드 등록
//  @Override
//  @Transactional
//  public KeywordDto registerKeyword(UUID userId, String keywordText) {
//    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
//    String normalizedKeyword = normalizeMultilingualText(keywordText);
//
//    Keyword keyword = Keyword.builder()
//        .user(user)
//        .keyword(normalizedKeyword)
//        .build();
//    keyword = keywordRepository.save(keyword);
//
//    // 비동기로 점수 계산 시작
//    calculateScoresAsync(keyword);
//
//    log.info("registerKeyword - 키워드 등록 완료: '{}' -> '{}', ID: {}",
//        keywordText, normalizedKeyword, keyword.getId());
//
//    return KeywordDto.from(keyword);
//  }
//
//  // 메인 추천 조회 메서드
//  @Override
//  @Transactional(readOnly = true)
//  public List<ContentDto> getRecommendationsByKeyword(
//      UUID keywordId,
//      UUID userId
//  ) {
//    // 키워드 권한 확인
//    Keyword keyword = keywordRepository.findByIdAndUserId(keywordId, userId)
//        .orElseThrow(KeywordNotFoundException::new);
//
//    // 점수가 계산되어 있는지 확인
//    if (!keywordContentRepository.existsByKeywordId(keywordId)) {
//      return handleMissingScores(keyword);
//    }
//
//    // 상위 30개 데이터 조회 (score 높은 순)
//    List<KeywordContent> top30Contents = keywordContentRepository
//        .findTop30ByKeywordIdOrderByScoreDesc(keywordId);
//
//    // ContentDto 리스트 생성
//    return top30Contents.stream()
//        .map(kc -> ContentDto.from(kc.getContent()))
//        .toList();
//  }
//
//  // 비동기 점수 계산
//  @Async("scoreCalculationExecutor")
//  @Transactional
//  public CompletableFuture<Void> calculateScoresAsync(Keyword keyword) {
//    try {
//      log.info("calculateScoreAsync- 키워드: '{}'", keyword.getKeyword());
//
//      // 기존 KeywordContent 삭제
//      keywordContentRepository.deleteByKeywordId(keyword.getId());
//
//      double threshold = getLanguageBasedThreshold(keyword.getKeyword());
//      int batchSize = 1000;
//      int offset = 0;
//      int totalProcessed = 0;
//      int totalSaved = 0;
//
//      while (true) {
//        List<Content> contentBatch = contentRepository.findAllWithPagination(offset, batchSize);
//
//        if (contentBatch.isEmpty()) {
//          break;
//        }
//
//        // 배치 처리 성능 개선
//        List<KeywordContent> keywordContents = processBatch(keyword, contentBatch, threshold);
//
//        if (!keywordContents.isEmpty()) {
//          keywordContentRepository.saveAll(keywordContents);
//          totalSaved += keywordContents.size();
//        }
//
//        totalProcessed += contentBatch.size();
//        offset += batchSize;
//
//        // 진행 상황 로깅
//        if (totalProcessed % 5000 == 0) {
//          log.info("calculateScoresAsync 진행 - 키워드: '{}', 처리: {}, 저장: {}",
//              keyword.getKeyword(), totalProcessed, totalSaved);
//        }
//      }
//
//      log.info("calculateScoresAsync 완료 - 키워드: '{}', 총 처리: {}, 총 저장: {}",
//          keyword.getKeyword(), totalProcessed, totalSaved);
//
//      return CompletableFuture.completedFuture(null);
//
//    } catch (Exception e) {
//      log.warn("점수 계산 실패 - 키워드: '{}'", keyword.getKeyword(), e);
//      // 실패 시 키워드 상태 업데이트 등 추가 처리 가능
//      return CompletableFuture.failedFuture(e);
//    }
//  }
//
//  private List<KeywordContent> processBatch(Keyword keyword, List<Content> contentBatch, double threshold) {
//    return contentBatch.parallelStream() // 병렬 처리로 성능 향상
//        .map(content -> {
//          double score = calculateAdvancedMatchingScore(keyword.getKeyword(), content);
//          return KeywordContent.builder()
//              .keyword(keyword)
//              .content(content)
//              .score(score)
//              .build();
//        })
//        .filter(kc -> kc.getScore() > threshold)
//        .toList();
//  }
//
//  // 점수가 없을 때 처리
//  private List<ContentDto> handleMissingScores(
//      Keyword keyword
//  ) {
//    log.warn("점수 계산이 완료되지 않음 - 키워드 ID: {}", keyword.getId());
//
//    // 빈 결과 반환
//    return List.of();
//  }
//
//  // 개선된 AI 매칭 점수 계산
//  private double calculateAdvancedMatchingScore(String keyword, Content content) {
//    double totalScore = 0.0;
//
//    // 1. 제목 매칭 (가중치 최적화)
//    double titleScore = calculateAdvancedTextSimilarity(keyword, content.getTitle()) * 0.5;
//
//    // 2. 설명 매칭
//    double descScore = calculateAdvancedTextSimilarity(keyword, content.getDescription()) * 0.3;
//
//    // 3. 장르/타입 매칭
//    double typeScore = calculateMultilingualTypeMatch(keyword, content.getContentType()) * 0.15;
//
//    // 4. 평점 보너스
//    double ratingBonus = content.getAvgRating() != null ?
//        content.getAvgRating().doubleValue() / 10.0 * 0.05 : 0.0;
//
//    totalScore = titleScore + descScore + typeScore + ratingBonus;
//    return Math.min(totalScore, 1.0);
//  }
//
//  // 개선된 텍스트 유사도 계산
//  private double calculateAdvancedTextSimilarity(String keyword, String text) {
//    if (text == null || keyword == null) return 0.0;
//
//    keyword = normalizeMultilingualText(keyword);
//    text = normalizeMultilingualText(text);
//
//    // 정확한 매칭
//    if (text.contains(keyword)) return 1.0;
//
//    // 1. 형태소 기반 매칭
//    double morphemeScore = calculateMorphemeSimilarity(keyword, text);
//
//    // 2. 동의어 매칭
//    double synonymScore = calculateSynonymSimilarity(keyword, text);
//
//    // 3. 번역어 매칭
//    double translationScore = calculateTranslationSimilarity(keyword, text);
//
//    // 4. 기본 단어 매칭
//    double basicScore = calculateBasicWordSimilarity(keyword, text);
//
//    // 가중 평균
//    return Math.min(
//        morphemeScore * 0.4 +
//            synonymScore * 0.3 +
//            translationScore * 0.2 +
//            basicScore * 0.1,
//        1.0
//    );
//  }
//
//  // 의미있는 형태소 추출
//  private List<String> extractMeaningfulMorphemes(String text) {
//    if (komoran == null || text == null || text.trim().isEmpty()) {
//      return Arrays.asList(text.split("\\s+"));
//    }
//
//    try {
//      KomoranResult result = komoran.analyze(text);
//      return result.getMorphesByTags("NNG", "NNP", "VV", "VA", "SL") // 명사, 동사, 형용사, 외국어
//          .stream()
//          .filter(morph -> morph.length() > 1) // 한 글자 제외
//          .filter(morph -> !KOREAN_STOPWORDS.contains(morph))
//          .distinct()
//          .collect(Collectors.toList());
//    } catch (Exception e) {
//      log.warn("형태소 추출 실패: {}", e.getMessage());
//      return Arrays.asList(text.split("\\s+"));
//    }
//  }
//
//  // 동의어 유사도 계산
//  private double calculateSynonymSimilarity(String keyword, String text) {
//    String[] keywordWords = keyword.split("\\s+");
//    String[] textWords = text.split("\\s+");
//
//    int matches = 0;
//    for (String kWord : keywordWords) {
//      for (String tWord : textWords) {
//        if (areSynonyms(kWord, tWord)) {
//          matches++;
//          break;
//        }
//      }
//    }
//
//    return keywordWords.length > 0 ? (double) matches / keywordWords.length : 0.0;
//  }
//
//  // 동의어 판별
//  private boolean areSynonyms(String word1, String word2) {
//    if (word1.equals(word2)) return true;
//
//    return SYNONYM_MAP.entrySet().stream()
//        .anyMatch(entry ->
//            (entry.getKey().equals(word1) && entry.getValue().contains(word2)) ||
//                (entry.getKey().equals(word2) && entry.getValue().contains(word1)) ||
//                (entry.getValue().contains(word1) && entry.getValue().contains(word2))
//        );
//  }
//
//  // 번역어 유사도 계산
//  private double calculateTranslationSimilarity(String keyword, String text) {
//    String[] keywordWords = keyword.split("\\s+");
//    String[] textWords = text.split("\\s+");
//
//    int matches = 0;
//    for (String kWord : keywordWords) {
//      for (String tWord : textWords) {
//        if (areTranslations(kWord, tWord)) {
//          matches++;
//          break;
//        }
//      }
//    }
//
//    return keywordWords.length > 0 ? (double) matches / keywordWords.length : 0.0;
//  }
//
//  // 번역어 판별
//  private boolean areTranslations(String word1, String word2) {
//    return (TRANSLATION_MAP.containsKey(word1) && TRANSLATION_MAP.get(word1).equals(word2)) ||
//        (TRANSLATION_MAP.containsKey(word2) && TRANSLATION_MAP.get(word2).equals(word1));
//  }
//
//  // 기본 단어 유사도 계산
//  private double calculateBasicWordSimilarity(String keyword, String text) {
//    String[] keywordWords = keyword.split("\\s+");
//    String[] textWords = text.split("\\s+");
//
//    int exactMatches = 0;
//    int partialMatches = 0;
//
//    for (String kWord : keywordWords) {
//      for (String tWord : textWords) {
//        if (kWord.equals(tWord)) {
//          exactMatches++;
//          break;
//        } else if (kWord.length() > 2 && tWord.length() > 2 &&
//            (kWord.contains(tWord) || tWord.contains(kWord))) {
//          partialMatches++;
//          break;
//        }
//      }
//    }
//
//    double exactScore = (double) exactMatches / keywordWords.length * 0.8;
//    double partialScore = (double) partialMatches / keywordWords.length * 0.4;
//
//    return Math.min(exactScore + partialScore, 1.0);
//  }
//
//  // 언어 감지
//  private String detectLanguage(String text) {
//    if (text == null || text.trim().isEmpty()) {
//      return "unknown";
//    }
//
//    boolean hasKorean = KOREAN_PATTERN.matcher(text).find();
//    boolean hasEnglish = ENGLISH_PATTERN.matcher(text).find();
//
//    if (hasKorean && hasEnglish) {
//      return "mixed";
//    } else if (hasKorean) {
//      return "korean";
//    } else if (hasEnglish) {
//      return "english";
//    } else {
//      return "other";
//    }
//  }
//
//  // 언어별 임계값 설정
//  private double getLanguageBasedThreshold(String text) {
//    String language = detectLanguage(text);
//    switch (language) {
//      case "korean":
//        return 0.2; // 한국어는 임계값 낮춤 (형태소 분석 활용)
//      case "english":
//        return 0.3;
//      case "mixed":
//        return 0.25;
//      default:
//        return 0.3;
//    }
//  }
//
//  // 다국어 텍스트 정규화
//  private String normalizeMultilingualText(String text) {
//    if (text == null) return "";
//
//    return text.toLowerCase()
//        .replaceAll("\\s+", " ")
//        .replaceAll("[!@#$%^&*(),.?\":{}|<>]", " ")
//        .trim();
//  }
//
//  // 다국어 콘텐츠 타입 매칭
//  private double calculateMultilingualTypeMatch(String keyword, ContentType contentType) {
//    keyword = keyword.toLowerCase();
//
//    switch (contentType) {
//      case MOVIE:
//        if (keyword.contains("영화") || keyword.contains("movie") || keyword.contains("film") ||
//            keyword.contains("시네마") || keyword.contains("cinema")) {
//          return 1.0;
//        }
//        break;
//      case TV:
//        if (keyword.contains("드라마") || keyword.contains("drama") || keyword.contains("시리즈") ||
//            keyword.contains("series") || keyword.contains("show")) {
//          return 1.0;
//        }
//        break;
//      case SPORTS:
//        if (keyword.contains("스포츠") || keyword.contains("sports") || keyword.contains("축구") ||
//            keyword.contains("야구") || keyword.contains("농구") || keyword.contains("경기") ||
//            keyword.contains("football") || keyword.contains("basketball") || keyword.contains("game")) {
//          return 1.0;
//        }
//        break;
//    }
//
//    return 0.0;
//  }
//
//  @Override
//  @Transactional
//  public List<KeywordDto> getKeywordsByUser(UUID userId) {
//    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
//
//    List<Keyword> keywords = keywordRepository.findAllByUserId(userId);
//    return keywords.stream().map(KeywordDto::from).toList();
//  }
//
//  @Override
//  @Transactional
//  public void batchCurationForNewContents(List<Content> newContents) {
//    if (newContents.isEmpty()) {
//      log.info("batchCurationForNewContents - 신규 콘텐츠가 없어 배치 큐레이션을 건너뜁니다.");
//      return;
//    }
//
//    List<Keyword> allKeywords = keywordRepository.findAll();
//
//    if (allKeywords.isEmpty()) {
//      log.info("batchCurationForNewContents - 등록된 키워드가 없어 배치 큐레이션을 건너뜁니다.");
//      return;
//    }
//
//    int totalProcessed = 0;
//    int totalSaved = 0;
//
//    for (Content content : newContents) {
//      for (Keyword keyword : allKeywords) {
//        double score = calculateAdvancedMatchingScore(keyword.getKeyword(), content);
//        double threshold = getLanguageBasedThreshold(keyword.getKeyword());
//
//        if (score > threshold) {
//          boolean exists = keywordContentRepository.existsByKeywordIdAndContentId(
//              keyword.getId(), content.getId());
//
//          if (!exists) {
//            KeywordContent keywordContent = KeywordContent.builder()
//                .keyword(keyword)
//                .content(content)
//                .score(score)
//                .build();
//            keywordContentRepository.save(keywordContent);
//            totalSaved++;
//          }
//        }
//        totalProcessed++;
//      }
//    }
//
//    log.info("batchCurationForNewContents - 신규 콘텐츠 {}개에 대한 배치 큐레이션 완료 - 처리: {}, 저장: {}",
//        newContents.size(), totalProcessed, totalSaved);
//  }
//
//  @Override
//  @Transactional
//  public void updateContentRating(UUID contentId) {
//    try {
//      Content content = contentRepository.findById(contentId)
//          .orElseThrow(ContentNotFoundException::new);
//
//      BigDecimal avgRating = getAvgRating(content);
//      content.setAvgRating(avgRating);
//      contentRepository.save(content);
//
//      log.info("updateContentRating - 콘텐츠 평점 업데이트 완료: {} -> {}", content.getTitle(), avgRating);
//    } catch (ContentNotFoundException e) {
//      log.warn("콘텐츠를 찾을 수 없음: {}", contentId);
//      throw e;
//    } catch (Exception e) {
//      log.warn("평점 업데이트 실패: contentId={}", contentId, e);
//      throw new ContentRatingUpdateException();
//    }
//  }
//
//  private BigDecimal getAvgRating(Content content) {
//    try {
//      List<ReviewDto> reviews = reviewService.getAllByContent(content.getId());
//
//      if (reviews.isEmpty()) {
//        return null;
//      }
//
//      double averageRating = reviews.stream()
//          .filter(review -> review.rating() != null)
//          .mapToDouble(review -> review.rating().doubleValue())
//          .average()
//          .orElse(0.0);
//
//      return BigDecimal.valueOf(averageRating).setScale(2, RoundingMode.HALF_UP);
//    } catch (Exception e) {
//      log.warn("평균 평점 계산 실패: contentId={}, error={}", content.getId(), e.getMessage());
//      throw e;
//    }
//  }
//
//  @Override
//  @Transactional
//  public void delete(UUID keywordId, UUID userId) {
//    Keyword keyword = keywordRepository.findById(keywordId)
//        .orElseThrow(KeywordNotFoundException::new);
//
//    if (!keyword.getUser().getId().equals(userId)) {
//      throw new KeywordDeleteDeniedException();
//    }
//
//    keywordRepository.delete(keyword);
//  }
//}

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
  public void delete(UUID keywordId, UUID userId) {
    Keyword keyword = keywordRepository.findById(keywordId)
            .orElseThrow(KeywordNotFoundException::new);

    if (!keyword.getUser().getId().equals(userId)) {
      throw new KeywordDeleteDeniedException();
    }

    keywordRepository.delete(keyword);
  }
}
