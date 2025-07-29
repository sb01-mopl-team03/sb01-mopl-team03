package team03.mopl.domain.curation.service;

import java.util.Set;
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
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.entity.ContentSearchResult;
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

  private final ContentSearchService contentSearchService;
  private final KeywordRepository keywordRepository;
  private final KeywordContentRepository keywordContentRepository;
  private final UserRepository userRepository;
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
        int matches = matchNewContentsWithKeywordUsingSearch(keyword, newContents);
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
   * OpenSearch를 활용한 새 콘텐츠와 키워드 매칭
   */
  private int matchNewContentsWithKeywordUsingSearch(Keyword keyword, List<Content> newContents) {
    String keywordText = keyword.getKeyword();

    // OpenSearch로 키워드와 관련된 콘텐츠 검색 (점수와 함께)
    List<ContentSearchResult> searchResults = contentSearchService.findContentsByKeywordWithScore(keywordText);

    if (searchResults.isEmpty()) {
      return 0;
    }

    // 새 콘텐츠 ID들을 Set으로 변환 (성능 최적화)
    Set<UUID> newContentIds = newContents.stream()
        .map(Content::getId)
        .collect(Collectors.toSet());

    // 검색 결과 중 새 콘텐츠에 해당하는 것들만 필터링
    List<ContentSearchResult> newContentResults = searchResults.stream()
        .filter(result -> newContentIds.contains(result.getContent().getId()))
        .collect(Collectors.toList());

    if (newContentResults.isEmpty()) {
      return 0;
    }

    // 기존에 이미 매칭된 콘텐츠들의 ID 조회 (중복 방지)
    List<UUID> existingContentIds = keywordContentRepository
        .findContentIdsByKeyword(keyword);

    // 새로운 매칭만 생성 (OpenSearch 점수 활용)
    List<KeywordContent> newKeywordContents = newContentResults.stream()
        .filter(result -> !existingContentIds.contains(result.getContent().getId()))
        .map(result -> {
          Double finalScore = calculateFinalRelevanceScore(result);
          return new KeywordContent(keyword, result.getContent(), finalScore);
        })
        .collect(Collectors.toList());

    if (!newKeywordContents.isEmpty()) {
      keywordContentRepository.saveAll(newKeywordContents);
      log.info("키워드 '{}' - {}개의 새로운 콘텐츠 매칭 추가 (OpenSearch 점수 활용)",
          keywordText, newKeywordContents.size());
    }

    return newKeywordContents.size();
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

    // 2. OpenSearch로 관련 콘텐츠 찾아서 자동 매핑 (점수와 함께)
    try {
      findAndMapRelatedContentsWithScore(savedKeyword);
    } catch (Exception e) {
      log.warn("키워드 '{}' 관련 콘텐츠 매핑 실패: {}", keywordText, e.getMessage());
      // TODO: 비동기로 재시도
    }

    return KeywordDto.from(savedKeyword);
  }

  /**
   * OpenSearch 점수를 활용하여 키워드와 관련된 콘텐츠들을 매핑
   */
  private void findAndMapRelatedContentsWithScore(Keyword keyword) {
    String keywordText = keyword.getKeyword();

    // OpenSearch로 관련 콘텐츠 검색 (점수와 함께)
    List<ContentSearchResult> searchResults = contentSearchService.findContentsByKeywordWithScore(keywordText);

    log.info("findAndMapRelatedContentsWithScore - 검색 결과 {}개", searchResults.size());
    if (searchResults.isEmpty()) {
      log.info("키워드 '{}'와 관련된 콘텐츠가 없습니다.", keywordText);
      return;
    }

    // KeywordContent 관계 생성 (OpenSearch 점수 활용)
    List<KeywordContent> keywordContents = searchResults.stream()
        .map(result -> {
          // OpenSearch 점수와 평점을 조합한 최종 점수 계산
          Double finalScore = calculateFinalRelevanceScore(result);
          return new KeywordContent(keyword, result.getContent(), finalScore);
        })
        .collect(Collectors.toList());

    // 데이터베이스에 저장
    keywordContentRepository.saveAll(keywordContents);

    log.info("키워드 '{}' - {}개의 관련 콘텐츠 매핑 완료 (OpenSearch 점수 활용)",
        keywordText, keywordContents.size());
  }

  /**
   * OpenSearch 점수와 콘텐츠 평점을 조합한 최종 관련성 점수 계산
   * @param searchResult OpenSearch에서 반환된 검색 결과 (점수 포함)
   * @return 0.0 ~ 1.0 사이의 정규화된 점수
   */
  private Double calculateFinalRelevanceScore(ContentSearchResult searchResult) {
    double searchScore = searchResult.getScore();
    Content content = searchResult.getContent();

    // 1. 선형 정규화 (최대 점수를 20으로 가정)
    double normalizedSearchScore = Math.min(searchScore / 20.0, 0.8);

    // 2. 평점 보너스
    double ratingBonus = 0.0;
    if (content.getAvgRating() != null && content.getAvgRating().doubleValue() > 0) {
      ratingBonus = (content.getAvgRating().doubleValue() / 5.0) * 0.2;
    }

    double finalScore = normalizedSearchScore + ratingBonus;

    return Math.min(finalScore, 1.0);
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

    log.info("키워드 '{}' 추천 콘텐츠 {}개 조회 (OpenSearch 점수 기반)",
        keyword.getKeyword(), recommendations.size());

    return recommendations;
  }

  public void delete(UUID keywordId, UUID userId) {
    Keyword keyword = keywordRepository.findByIdAndUserId(keywordId, userId)
        .orElseThrow(KeywordDeleteDeniedException::new);

    keywordRepository.delete(keyword); // KeywordContent는 CASCADE로 자동 삭제
    log.info("키워드 삭제: {}", keyword.getKeyword());
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
