package team03.mopl.domain.curation.service;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.entity.KeywordContent;
import team03.mopl.domain.curation.repository.KeywordContentRepository;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ContentRecommendServiceImpl implements ContentRecommendService {
  private final ContentRepository contentRepository;
  private final KeywordRepository keywordRepository;
  private final KeywordContentRepository keywordContentRepository;
  private final UserRepository userRepository;
  private StanfordCoreNLP nlpPipeline;

  @PostConstruct
  @Override
  public void init() {
    initializeAI();
  }

  // 실제 AI 모델 초기화
  private void initializeAI() {
    try {
      Properties props = new Properties();
      props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
      props.setProperty("ner.useSUTime", "false");
      this.nlpPipeline = new StanfordCoreNLP(props);
      log.info("AI 콘텐츠 추천 엔진 초기화 완료");
    } catch (Exception e) {
      log.error("AI 모델 초기화 실패, 기본 모드로 실행: " + e.getMessage());
      this.nlpPipeline = null;
    }
  }

  // 사용자 키워드 등록
  @Override
  public Keyword registerKeyword(UUID userId, String keywordText) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    Keyword keyword = new Keyword(user, keywordText);
    keyword = keywordRepository.save(keyword);

    // AI로 해당 키워드에 맞는 콘텐츠 자동 큐레이션
    List<Content> recommendations = curateContentForKeyword(keyword);

    log.info("키워드 등록: '{}' (매칭된 콘텐츠: {}개)", keywordText, recommendations.size());
    return keyword;
  }

  // AI 기반 키워드별 콘텐츠 큐레이션
  @Override
  public List<Content> curateContentForKeyword(Keyword keyword) {
    String keywordText = keyword.getKeyword();
    List<Content> recommendations = new ArrayList<>();
    List<Content> allContents = contentRepository.findAll();

    // 각 콘텐츠에 대해 AI 점수 계산
    for (Content content : allContents) {
      double aiScore = calculateAIMatchingScore(keywordText, content);

      // 임계값 이상의 콘텐츠만 추천 (0.3 이상)
      if (aiScore > 0.3) {
        recommendations.add(content);

        // KeywordContent 테이블에 큐레이션 결과 저장
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

  // AI 매칭 점수 계산
  private double calculateAIMatchingScore(String keyword, Content content) {
    double totalScore = 0.0;

    // 1. 제목 매칭 (40% 가중치)
    double titleScore = calculateTextSimilarity(keyword, content.getTitle()) * 0.4;

    // 2. 설명 매칭 (30% 가중치)
    double descScore = calculateTextSimilarity(keyword, content.getDescription()) * 0.3;

    // 3. 장르/타입 매칭 (20% 가중치)
    double typeScore = calculateTypeMatch(keyword, content.getContentType()) * 0.2;

    // 4. 평점 보너스 (10% 가중치)
    double ratingBonus = content.getAvgRating() != null ?
        content.getAvgRating().doubleValue() / 10.0 * 0.1 : 0.0;

    totalScore = titleScore + descScore + typeScore + ratingBonus;

    return Math.min(totalScore, 1.0); // 최대 1.0으로 제한
  }

  // 텍스트 유사도 계산 (AI 기반)
  private double calculateTextSimilarity(String keyword, String text) {
    if (text == null || keyword == null) return 0.0;

    keyword = keyword.toLowerCase();
    text = text.toLowerCase();

    // 정확한 매칭
    if (text.contains(keyword)) return 1.0;

    // AI 모델이 있으면 NLP 분석 사용
    if (nlpPipeline != null) {
      return calculateNLPSimilarity(keyword, text);
    }

    // 기본 유사도 계산
    return calculateBasicSimilarity(keyword, text);
  }

  // NLP 기반 유사도 계산
  private double calculateNLPSimilarity(String keyword, String text) {
    try {
      List<String> keywordTokens = extractKeywordsWithAI(keyword);
      List<String> textTokens = extractKeywordsWithAI(text);

      int matches = 0;
      for (String kToken : keywordTokens) {
        for (String tToken : textTokens) {
          if (kToken.equals(tToken) || areSemanticallySimilar(kToken, tToken)) {
            matches++;
          }
        }
      }
      return keywordTokens.isEmpty() ? 0.0 : (double) matches / keywordTokens.size();
    } catch (Exception e) {
      log.warn("NLP 분석 실패, 기본 유사도 게산 사용: {}", e.getMessage());
      return calculateBasicSimilarity(keyword, text);
    }
  }

  // AI로 키워드 추출
  private List<String> extractKeywordsWithAI(String text) {
    try {
      CoreDocument document = new CoreDocument(text);
      nlpPipeline.annotate(document);

      Set<String> keywords = new HashSet<>();

      // NER로 중요한 엔티티 추출
      for (CoreEntityMention entity : document.entityMentions()) {
        keywords.add(entity.text().toLowerCase());
      }

      // POS 태깅으로 명사, 형용사 추출
      for (CoreSentence sentence : document.sentences()) {
        for (CoreLabel token : sentence.tokens()) {
          String word = token.lemma().toLowerCase();
          String pos = token.get(PartOfSpeechAnnotation.class);

          if (isImportantPOS(pos) && word.length() > 2) {
            keywords.add(word);
          }
        }
      }
      return new ArrayList<>(keywords);
    } catch (Exception e) {
      log.warn("AI 키워드 추출 실패, 기본 분할 사용: {}", e.getMessage());
      return Arrays.asList(text.toLowerCase().split("\\s+"));
    }
  }

  // 품사 중요도 판별
  private boolean isImportantPOS(String pos) {
    return pos.startsWith("NN") || pos.startsWith("JJ") || pos.startsWith("VB");
  }

  // 의미적 유사성 판별
  private boolean areSemanticallySimilar(String word1, String word2) {
    Map<String, Set<String>> semanticGroups = createEntertainmentSemanticGroups();

    for (Set<String> group : semanticGroups.values()) {
      if (group.contains(word1) && group.contains(word2)) {
        return true;
      }
    }

    return false;
  }

  // 엔터테인먼트 특화 의미 그룹
  private Map<String, Set<String>> createEntertainmentSemanticGroups() {
    Map<String, Set<String>> groups = new HashMap<>();

    groups.put("action", Set.of("액션", "action", "어벤져스", "슈퍼히어로", "hero", "전투", "fight"));
    groups.put("drama", Set.of("드라마", "drama", "연기", "배우", "actor", "감동", "emotion"));
    groups.put("comedy", Set.of("코미디", "comedy", "웃음", "유머", "humor", "재미", "funny"));
    groups.put("thriller", Set.of("스릴러", "thriller", "서스펜스", "suspense", "긴장", "tension"));
    groups.put("romance", Set.of("로맨스", "romance", "사랑", "love", "연애", "romantic"));
    groups.put("sports", Set.of("스포츠", "sports", "축구", "football", "농구", "basketball", "올림픽", "olympic"));
    groups.put("korean", Set.of("한국", "korean", "k-drama", "한류", "hallyu"));

    return groups;
  }

  // 기본 유사도 계산
  private double calculateBasicSimilarity(String keyword, String text) {
    String[] keywordWords = keyword.split("\\s+");
    int matches = 0;

    for (String word : keywordWords) {
      if (text.contains(word)) {
        matches++;
      }
    }

    return keywordWords.length > 0 ? (double) matches / keywordWords.length : 0.0;
  }

  // 콘텐츠 타입 매칭
  private double calculateTypeMatch(String keyword, ContentType contentType) {
    keyword = keyword.toLowerCase();

    switch (contentType) {
      case MOVIE:
        if (keyword.contains("영화") || keyword.contains("movie") ||
            keyword.contains("film") || keyword.contains("cinema")) return 1.0;
        break;
      case DRAMA:
        if (keyword.contains("드라마") || keyword.contains("drama") ||
            keyword.contains("시리즈") || keyword.contains("series")) return 1.0;
        break;
      case SPORTS:
        if (keyword.contains("스포츠") || keyword.contains("sports") ||
            keyword.contains("축구") || keyword.contains("농구") ||
            keyword.contains("올림픽") || keyword.contains("월드컵")) return 1.0;
        break;
    }

    return 0.0;
  }

  // 사용자별 추천 콘텐츠 조회
  @Override
  public List<Content> getRecommendationsForUser(UUID userId, int limit) {
    // 사용자의 모든 키워드 가져오기
   List<Keyword> userKeywords = keywordRepository.findAllByUserId(userId);

    if (userKeywords.isEmpty()) {
      log.info("사용자 {}의 등록된 키워드가 없습니다.", userId);
      return Collections.emptyList();
    }

    // KeywordContent에서 해당 사용자의 큐레이션 결과 조회
    Set<Content> recommendedContents = new HashSet<>();

    for (Keyword keyword : userKeywords) {
      List<KeywordContent> keywordContents = keywordContentRepository.findByKeywordId(keyword.getId());

      for (KeywordContent kc : keywordContents) {
        recommendedContents.add(kc.getContent());
      }
    }

    // 점수별로 정렬하여 상위 콘텐츠 반환
    return recommendedContents.stream()
        .sorted((c1, c2) -> {
          java.math.BigDecimal rating1 = c1.getAvgRating() != null ? c1.getAvgRating() : java.math.BigDecimal.ZERO;
          java.math.BigDecimal rating2 = c2.getAvgRating() != null ? c2.getAvgRating() : java.math.BigDecimal.ZERO;
          return rating2.compareTo(rating1);
        })
        .limit(limit)
        .collect(Collectors.toList());
  }

  // 배치 큐레이션 (새 콘텐츠에 대해 기존 키워드 매칭)
  @Override
  public void batchCurationForNewContents(List<Content> newContents) {
    List<Keyword> allKeywords = keywordRepository.findAll();

    for (Content content : newContents) {
      for (Keyword keyword : allKeywords) {
        double score = calculateAIMatchingScore(keyword.getKeyword(), content);

        if (score > 0.3) {
          // 중복 체크 후 저장
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
}