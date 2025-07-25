package team03.mopl.domain.curation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.curation.KeywordAccessDeniedException;
import team03.mopl.common.exception.curation.KeywordDeleteDeniedException;
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.elasticsearch.ContentSearchService;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.entity.KeywordContent;
import team03.mopl.domain.curation.repository.KeywordContentRepository;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurationService {

  private final KeywordRepository keywordRepository;
  private final KeywordContentRepository keywordContentRepository;
  private final UserRepository userRepository;
  private final ContentSearchService contentSearchService; // Elasticsearch 검색 서비스
  private final ContentRepository contentRepository;

  /**
   * 새로운 콘텐츠들을 기존 키워드들과 매칭하는 배치 큐레이션
   * @param newContents 새로 추가된 콘텐츠 목록
   */
  public void batchCurationForNewContents(List<Content> newContents) {
    if (newContents == null || newContents.isEmpty()) {
      log.info("batchCurationForNewContents - 처리할 새 콘텐츠가 없습니다.");
      return;
    }

    log.info("batchCurationForNewContents 시작 - 새 콘텐츠 {}개", newContents.size());

    try {
      // 1. 먼저 새 콘텐츠들을 Elasticsearch에 인덱싱
      indexNewContents(newContents);

      // 2. 모든 기존 키워드들 조회
      List<Keyword> allKeywords = keywordRepository.findAll();

      if (allKeywords.isEmpty()) {
        log.info("batchCurationForNewContents - 기존 키워드가 없어 매칭을 건너뜁니다.");
        return;
      }

      log.info("batchCurationForNewContents - {}개 키워드와 매칭 시작", allKeywords.size());

      // 3. 각 키워드별로 새 콘텐츠들과의 매칭 수행
      int totalMatches = 0;
      for (Keyword keyword : allKeywords) {
        int matches = matchNewContentsWithKeyword(keyword, newContents);
        totalMatches += matches;
      }

      log.info("batchCurationForNewContents 완료 - 총 {}개의 새로운 매칭 생성", totalMatches);

    } catch (Exception e) {
      log.warn("batchCurationForNewContents 실패", e);
      throw new RuntimeException("새 콘텐츠 큐레이션 배치 작업 실패", e);
    }
  }

  /**
   * 새 콘텐츠들을 Elasticsearch에 인덱싱
   */
  private void indexNewContents(List<Content> newContents) {
    log.info("새 콘텐츠 {}개를 Elasticsearch에 인덱싱 시작", newContents.size());

    for (Content content : newContents) {
      try {
        contentSearchService.indexContent(content);
      } catch (Exception e) {
        log.warn("콘텐츠 인덱싱 실패 - ID: {}, 제목: {}", content.getId(), content.getTitle(), e);
      }
    }

    log.info("새 콘텐츠 Elasticsearch 인덱싱 완료");
  }

  /**
   * 특정 키워드와 새 콘텐츠들 간의 매칭 수행
   */
  private int matchNewContentsWithKeyword(Keyword keyword, List<Content> newContents) {
    String keywordText = keyword.getKeyword();

    // 새 콘텐츠 중에서 이 키워드와 관련된 것들만 필터링
    List<Content> matchedContents = newContents.stream()
        .filter(content -> isContentRelevantToKeyword(keywordText, content))
        .collect(Collectors.toList());

    if (matchedContents.isEmpty()) {
      return 0;
    }

    // 기존에 이미 매칭된 콘텐츠들의 ID 조회 (중복 방지)
    List<UUID> existingContentIds = keywordContentRepository
        .findContentIdsByKeyword(keyword);

    // 새로운 매칭만 생성
    List<KeywordContent> newKeywordContents = matchedContents.stream()
        .filter(content -> !existingContentIds.contains(content.getId()))
        .map(content -> {
          Double score = calculateRelevanceScore(keywordText, content);
          return new KeywordContent(keyword, content, score);
        })
        .collect(Collectors.toList());

    if (!newKeywordContents.isEmpty()) {
      keywordContentRepository.saveAll(newKeywordContents);
      log.info("키워드 '{}' - {}개의 새로운 콘텐츠 매칭 추가",
          keywordText, newKeywordContents.size());
    }

    return newKeywordContents.size();
  }

  /**
   * 콘텐츠가 키워드와 관련이 있는지 판단
   * (Elasticsearch 검색 결과를 사용하지 않고 직접 판단)
   */
  private boolean isContentRelevantToKeyword(String keyword, Content content) {
    String keywordLower = keyword.toLowerCase();
    String title = content.getTitle().toLowerCase();
    String description = content.getDescription() != null ?
        content.getDescription().toLowerCase() : "";

    // 제목이나 설명에 키워드가 포함되어 있으면 관련있다고 판단
    return title.contains(keywordLower) || description.contains(keywordLower);
  }

  @Transactional
  public KeywordDto registerKeyword(UUID userId, String keywordText) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    // 1. 키워드 저장
    Keyword keyword = Keyword.builder()
        .user(user)
        .keyword(keywordText)
        .build();

    Keyword savedKeyword = keywordRepository.save(keyword);
    log.info("키워드 등록: 사용자={}, 키워드={}", user.getEmail(), keywordText);

    // 2. Elasticsearch로 관련 콘텐츠 찾아서 자동 매핑
    try {
      findAndMapRelatedContents(savedKeyword);
    } catch (Exception e) {
      log.warn("키워드 '{}' 관련 콘텐츠 매핑 실패: {}", keywordText, e.getMessage());
      // TODO: 비동기로 재시도
    }

    return KeywordDto.from(savedKeyword);
  }

  /**
   * 키워드와 관련된 콘텐츠들을 Elasticsearch로 찾아서 KeywordContent 테이블에 저장
   */
  private void findAndMapRelatedContents(Keyword keyword) {
    String keywordText = keyword.getKeyword();

    // Elasticsearch로 관련 콘텐츠 검색
    List<Content> relatedContents = contentSearchService.findContentsByKeyword(keywordText);

    log.info("findAndMapRelatedContents - relatedCOntents 개수 = {}", relatedContents.size());
    if (relatedContents.isEmpty()) {
      log.info("키워드 '{}'와 관련된 콘텐츠가 없습니다.", keywordText);
      return;
    }

    // KeywordContent 관계 생성 (점수와 함께)
    List<KeywordContent> keywordContents = relatedContents.stream()
        .map(content -> {
          // 관련성 점수 계산 (제목에 키워드 포함 시 높은 점수)
          Double score = calculateRelevanceScore(keywordText, content);
          return new KeywordContent(keyword, content, score);
        })
        .collect(Collectors.toList());

    // 데이터베이스에 저장
    log.info("키워드 개수", keywordContents.size());
    keywordContentRepository.saveAll(keywordContents);

    log.info("키워드 '{}' - {}개의 관련 콘텐츠 매핑 완료", keywordText, keywordContents.size());
  }

  /**
   * 키워드별 추천 콘텐츠 조회 (점수 순으로 정렬)
   */
  @Transactional(readOnly = true)
  public List<ContentDto> getRecommendationsByKeyword(UUID keywordId, UUID userId) {
    // 키워드 소유권 확인
    Keyword keyword = keywordRepository.findByIdAndUserId(keywordId, userId)
        .orElseThrow(KeywordAccessDeniedException::new);

    // 관련성 점수 순으로 콘텐츠 조회
    List<KeywordContent> keywordContents = keywordContentRepository
        .findByKeywordOrderByScoreDesc(keyword);

    List<ContentDto> recommendations = keywordContents.stream()
        .map(kc -> {
          ContentDto dto = ContentDto.from(kc.getContent());
          // 추천 점수도 포함할 수 있음
          return dto;
        })
        .collect(Collectors.toList());

    log.info("키워드 '{}' 추천 콘텐츠 {}개 조회", keyword.getKeyword(), recommendations.size());

    return recommendations;
  }

  /**
   * 키워드 삭제
   */
  public void delete(UUID keywordId, UUID userId) {
    Keyword keyword = keywordRepository.findByIdAndUserId(keywordId, userId)
        .orElseThrow(KeywordDeleteDeniedException::new);

    keywordRepository.delete(keyword); // KeywordContent는 CASCADE로 자동 삭제
    log.info("키워드 삭제: {}", keyword.getKeyword());
  }

  /**
   * 관련성 점수 계산
   * - 제목에 키워드 포함: 높은 점수
   * - 설명에만 포함: 중간 점수
   * - 평점도 고려
   */
  private Double calculateRelevanceScore(String keyword, Content content) {
    double score = 0.0;

    String title = content.getTitle().toLowerCase();
    String description = content.getDescription() != null ?
        content.getDescription().toLowerCase() : "";
    String keywordLower = keyword.toLowerCase();

    // 제목에 키워드 포함 시 높은 점수
    if (title.contains(keywordLower)) {
      score += 0.6;
    }

    // 설명에 키워드 포함 시 중간 점수
    if (description.contains(keywordLower)) {
      score += 0.3;
    }

    // 평점 보너스 (5점 만점 기준으로 0.1 추가 점수)
    double ratingBonus = content.getAvgRating().doubleValue() / 5.0 * 0.1;
    score += ratingBonus;

    return Math.min(score, 1.0); // 최대 1.0점
  }

  public List<KeywordDto> getKeywordsByUser(UUID userId) {
    log.info("getKeywordsByUser - 사용자 키워드 조회: userId={}", userId);

    try {
      // 사용자의 모든 키워드 조회
      List<Keyword> keywords = keywordRepository.findByUserIdOrderByCreatedAtDesc(userId);

      List<KeywordDto> keywordDtos = keywords.stream()
          .map(KeywordDto::from)
          .toList();

      log.info("getKeywordsByUser - 사용자 {}의 키워드 {}개 조회 완료", userId, keywordDtos.size());
      return keywordDtos;

    } catch (Exception e) {
      log.warn("사용자 키워드 조회 실패: userId={}, error={}", userId, e.getMessage());
      throw new KeywordNotFoundException();
    }
  }

  @Transactional(readOnly = true)
  public void reindexAllContentsToElasticsearch() {
    log.info("Elasticsearch 전체 재색인 시작...");
    List<Content> allContents = contentRepository.findAll(); // 모든 콘텐츠를 DB에서 가져옴
    log.info("DB에서 {}개의 콘텐츠 조회. Elasticsearch에 인덱싱 시작.", allContents.size());

    for (Content content : allContents) {
      try {
        contentSearchService.indexContent(content);
      } catch (Exception e) {
        log.error("Elasticsearch 인덱싱 실패: ID={}, 제목={}", content.getId(), content.getTitle(), e);
      }
    }
    log.info("Elasticsearch 전체 재색인 완료.");
  }
}
