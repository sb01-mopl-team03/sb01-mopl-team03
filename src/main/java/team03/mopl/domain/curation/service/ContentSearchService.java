package team03.mopl.domain.curation.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.SortOrder;
import org.opensearch.client.opensearch._types.query_dsl.Operator;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.entity.ContentSearch;
import team03.mopl.domain.curation.entity.ContentSearchResult;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSearchService {

  private final OpenSearchClient openSearchClient;
  private final ContentRepository contentRepository;

  private String contentIndex = "mopl-contents";

  @PostConstruct
  public void initializeIndex() {
    log.info("ContentSearchService 인덱스 초기화 시작");

    try {
      log.debug("OpenSearch 연결 테스트 중...");
      try {
        var healthResponse = openSearchClient.cluster().health();
        log.info("OpenSearch 클러스터 연결 성공 - 상태: {}", healthResponse.status());
      } catch (Exception e) {
        log.error("OpenSearch 클러스터 연결 실패", e);
        log.info("인덱스 초기화를 건너뜁니다. 애플리케이션은 검색 기능 없이 계속 실행됩니다.");
        return;
      }

      // 인덱스 존재 여부 확인 및 생성
      boolean exists = openSearchClient.indices().exists(ExistsRequest.of(e -> e.index(contentIndex))).value();

      if (!exists) {
        log.info("인덱스 '{}'가 존재하지 않아 새로 생성 및 모든 콘텐츠 인덱싱 시작.", contentIndex);
        createIndexIfNotExists(); // 인덱스 생성
        reindexAllContents(); // 생성 후 모든 콘텐츠 인덱싱
      } else {
        // 인덱스는 존재하지만 문서 수가 0인 경우도 초기화/재인덱싱 고려
        // 또는 관리자가 명시적으로 재인덱싱을 요청할 때만 이 로직을 사용
        long docCount = openSearchClient.search(s -> s.index(contentIndex).size(0), ContentSearch.class)
            .hits().total().value();
        if (docCount == 0) {
          log.warn("인덱스 '{}'는 존재하지만 문서 수가 0입니다. 모든 콘텐츠를 재인덱싱합니다.", contentIndex);
          reindexAllContents(); // 문서가 없으면 재인덱싱
        } else {
          log.info("인덱스 '{}'가 존재하고 문서가 있어 초기화 과정을 건너뜁니다. (총 문서 수: {})", contentIndex, docCount);
        }
      }

      log.info("ContentSearchService 인덱스 초기화 완료");

    } catch (Exception e) {
      log.error("ContentSearchService 인덱스 초기화 중 오류 발생", e);
      log.info("인덱스 초기화 실패했지만 애플리케이션은 계속 실행됩니다. 검색 기능이 제한될 수 있습니다.");
    }
  }

  private void createIndexIfNotExists() throws IOException {
    try {
      log.debug("인덱스 '{}' 존재 여부 확인 시작", contentIndex);

      // 인덱스 존재 여부 확인
      boolean exists = openSearchClient.indices().exists(ExistsRequest.of(e -> e.index(contentIndex))).value();

      if (exists) {
        log.info("인덱스 '{}' 이미 존재함", contentIndex);
        return;
      }

      log.info("인덱스 '{}' 생성 시작", contentIndex);

      // 인덱스 생성
      CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
          .index(contentIndex)
          .mappings(m -> m
              .properties("id", p -> p.keyword(k -> k))
              .properties("title", p -> p.text(t -> t.analyzer("standard")))
              .properties("description", p -> p.text(t -> t.analyzer("standard")))
              .properties("content_type", p -> p.keyword(k -> k))
              .properties("avg_rating", p -> p.double_(d -> d))
          )
      );

      var response = openSearchClient.indices().create(createIndexRequest);

      if (response.acknowledged()) {
        log.info("인덱스 '{}' 생성 완료", contentIndex);
      } else {
        log.warn("인덱스 '{}' 생성 응답에서 acknowledged가 false입니다. 수동으로 확인이 필요할 수 있습니다.", contentIndex);
      }

    } catch (Exception e) {
      log.error("인덱스 생성 중 오류 발생: {}", e.getMessage());

      // 상세한 오류 정보 로깅
      if (e.getCause() != null) {
        log.error("근본 원인: {}", e.getCause().getMessage());
      }

      // 스택 트레이스는 DEBUG 레벨로
      log.debug("인덱스 생성 오류 상세 스택 트레이스", e);

      throw e;
    }
  }

  /**
   * 단일 콘텐츠 인덱싱 - ContentSearch DTO 사용으로 통일
   */
  public void indexContent(Content content) {
    try {
      ContentSearch document = ContentSearch.from(content);

      IndexRequest<ContentSearch> request = IndexRequest.of(i -> i
          .index(contentIndex)
          .id(content.getId().toString())
          .document(document)
      );

      // OpenSearch 작업 실행
      openSearchClient.index(request); // 이 부분에서 IOException 발생 가능
      log.debug("콘텐츠 인덱싱 완료: ID={}, 제목={}", content.getId(), content.getTitle());

    } catch (IOException e) { // OpenSearch 관련 예외를 여기서 잡습니다.
      log.warn("콘텐츠 인덱싱 실패: ID={}, 제목={}", content.getId(), content.getTitle(), e);
      // 실패 시 추가적인 로직 (예: 재시도 큐에 추가, 사용자에게 알림)
      throw new RuntimeException("콘텐츠 인덱싱 실패", e); // 필요에 따라 런타임 예외로 래핑하여 상위로 전파
    }
  }

  /**
   * 배치 인덱싱 - ContentSearch DTO 사용으로 통일
   */
  public void batchIndexContents(List<Content> contents) {
    if (contents == null || contents.isEmpty()) {
      log.info("batchIndexContents - 인덱싱할 콘텐츠가 없습니다.");
      return;
    }

    log.info("batchIndexContents - 배치 인덱싱 시작: {}개", contents.size());

    try {
      var bulkRequestBuilder = new org.opensearch.client.opensearch.core.BulkRequest.Builder();

      for (Content content : contents) {
        ContentSearch searchDto = ContentSearch.from(content);

        bulkRequestBuilder.operations(op -> op
            .index(idx -> idx
                .index(contentIndex)
                .id(content.getId().toString())
                .document(searchDto)
            )
        );
      }

      var bulkResponse = openSearchClient.bulk(bulkRequestBuilder.build());

      // bulkResponse.errors()를 통해 전체 작업 중 오류가 있었는지 확인
      if (bulkResponse.errors()) {
        long errorCount = bulkResponse.items().stream()
            .mapToLong(item -> item.error() != null ? 1 : 0)
            .sum();

        log.warn("batchIndexContents - 배치 인덱싱 일부 실패: 성공 {}개, 실패 {}개",
            contents.size() - errorCount, errorCount);

        // 각 항목별 실패 원인 확인 및 로깅
        bulkResponse.items().forEach(item -> {
          if (item.error() != null) {
            log.warn("배치 인덱싱 실패 항목 - ID: {}, 오류: {}",
                item.id(), item.error().reason());
            // 여기서 개별 실패 항목에 대한 후속 처리 (예: 재처리 목록에 추가)
          }
        });
      } else {
        log.info("batchIndexContents - 배치 인덱싱 성공: {}개", contents.size());
      }

    } catch (IOException e) {
      log.error("batchIndexContents - 배치 인덱싱 실패", e);
      throw new RuntimeException("배치 인덱싱 실패", e);
    }
  }

  /**
   * 키워드로 콘텐츠 검색 - ContentSearchResult 반환으로 통일
   */
  public List<ContentSearchResult> findContentsByKeywordWithScore(String keyword) {
    return findContentsByKeywordWithScore(keyword, 100, 0);
  }

  public List<ContentSearchResult> findContentsByKeywordWithScore(String keyword, int size, int from) {
    log.info("=== 검색 시작 ===");
    log.info("키워드: '{}', size: {}, from: {}", keyword, size, from);

    try {
      checkIndexStatusForSearch();

      // 더 균형잡힌 검색 쿼리
      Query searchQuery = Query.of(q -> q
          .bool(b -> b
              .should(s -> s
                  // 1. 정확한 단어 매칭 (가장 높은 가중치)
                  .match(m -> m
                      .field("title")
                      .query(FieldValue.of(keyword))
                      .boost(5.0f)
                      .operator(Operator.And) // 정확 매칭 강화
                  )
              )
              .should(s -> s
                  // 2. 설명에서 정확한 단어 매칭
                  .match(m -> m
                      .field("description")
                      .query(FieldValue.of(keyword))
                      .boost(3.0f)
                      .operator(Operator.And)
                  )
              )
              .should(s -> s
                  // 3. 제목에서 부분 매칭 (와일드카드)
                  .wildcard(w -> w
                      .field("title")
                      .value("*" + keyword + "*")
                      .boost(4.0f)
                  )
              )
              .should(s -> s
                  // 4. 설명에서 부분 매칭 (와일드카드)
                  .wildcard(w -> w
                      .field("description")
                      .value("*" + keyword + "*")
                      .boost(2.5f)
                  )
              )
              .should(s -> s
                  // 5. 구문 검색 (phrase matching) - 맥락 보존
                  .matchPhrase(mp -> mp
                      .field("description")
                      .query(keyword)
                      .boost(3.5f)
                  )
              )
              .should(s -> s
                  // 6. 퍼지 매칭 (오타 허용, 가중치 낮음)
                  .fuzzy(f -> f
                      .field("title")
                      .value(FieldValue.of(keyword))
                      .fuzziness("1") // AUTO 대신 1로 제한
                      .boost(1.0f)
                  )
              )
              .should(s -> s
                  // 7. 설명에서 퍼지 매칭
                  .fuzzy(f -> f
                      .field("description")
                      .value(FieldValue.of(keyword))
                      .fuzziness("1")
                      .boost(0.8f)
                  )
              )
              // 최소 1개 조건 만족
              .minimumShouldMatch("1")
          )
      );

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index(contentIndex)
          .query(searchQuery)
          .size(size)
          .from(from)
          .sort(sort -> sort.score(sc -> sc.order(SortOrder.Desc)))
      );

      log.info("개선된 검색 쿼리 사용 - 키워드: '{}'", keyword);

      SearchResponse<ContentSearch> searchResponse = openSearchClient.search(searchRequest, ContentSearch.class);

      log.info("키워드 '{}' 검색 완료: {}개 매칭", keyword, searchResponse.hits().total().value());

      List<ContentSearchResult> results = new ArrayList<>();

      for (Hit<ContentSearch> hit : searchResponse.hits().hits()) {
        try {
          UUID contentId = UUID.fromString(hit.id());
          Content content = contentRepository.findById(contentId).orElse(null);

          if (content != null) {
            double score = hit.score() != null ? hit.score() : 0.0;
            results.add(new ContentSearchResult(content, score));
          }
        } catch (Exception e) {
          log.warn("검색 결과 처리 중 오류: hitId={}", hit.id(), e);
        }
      }

      log.info("키워드 '{}' 검색 결과: {}개 반환", keyword, results.size());

      return results;

    } catch (IOException e) {
      log.error("OpenSearch 검색 실패: keyword={}", keyword, e);
      return new ArrayList<>();
    }
  }

  /**
   * 키워드 주변 맥락 추출
   */
  private String extractKeywordContext(String text, String keyword, int contextLength) {
    int keywordIndex = text.toLowerCase().indexOf(keyword.toLowerCase());
    if (keywordIndex == -1) return "";

    int start = Math.max(0, keywordIndex - contextLength);
    int end = Math.min(text.length(), keywordIndex + keyword.length() + contextLength);

    return text.substring(start, end).trim();
  }

  /**
   * 검색용 간단한 인덱스 상태 확인
   */
  private void checkIndexStatusForSearch() {
    try {
      ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(contentIndex));
      boolean indexExists = openSearchClient.indices().exists(existsRequest).value();

      log.info("인덱스 '{}' 존재: {}", contentIndex, indexExists);

      if (indexExists) {
        SearchRequest countRequest = SearchRequest.of(s -> s
            .index(contentIndex)
            .size(0)
        );

        SearchResponse<ContentSearch> countResponse = openSearchClient.search(countRequest, ContentSearch.class);
        log.info("인덱스 총 문서 수: {}", countResponse.hits().total().value());
      }
    } catch (IOException e) {
      log.warn("인덱스 상태 확인 실패", e);
    }
  }

//  /**
//   * 인덱스 생성 - ContentSearch 필드에 맞춰 매핑 정의
//   */
//  private void createIndexIfNotExists() throws IOException {
//    ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(contentIndex));
//
//    if (!openSearchClient.indices().exists(existsRequest).value()) {
//      CreateIndexRequest createIndexRequest = CreateIndexRequest.of(c -> c
//          .index(contentIndex)
//          .mappings(m -> m
//              .properties("id", p -> p.keyword(k -> k))
//              .properties("title", p -> p.text(t -> t.analyzer("standard")))
//              .properties("description", p -> p.text(t -> t.analyzer("standard")))
//              .properties("content_type", p -> p.keyword(k -> k))  // String으로 저장됨
//              .properties("avg_rating", p -> p.double_(d -> d))
//          )
//      );
//
//      openSearchClient.indices().create(createIndexRequest);
//      log.info("OpenSearch 인덱스 '{}' 생성 완료", contentIndex);
//    }
//  }

  /**
   * 인덱스 초기화 - 삭제 후 재생성하고 모든 콘텐츠 재인덱싱
   */
  @Transactional(readOnly = true)
  public void initializeIndexWithAllContents() {
    log.info("=== 인덱스 초기화 시작 ===");

    try {
      // 1. 기존 인덱스 삭제
      deleteIndexIfExists();

      // 2. 새 인덱스 생성
      createIndexIfNotExists();

      // 3. 모든 콘텐츠 재인덱싱
      reindexAllContents();

      log.info("=== 인덱스 초기화 완료 ===");

    } catch (Exception e) {
      log.error("인덱스 초기화 실패", e);
      throw new RuntimeException("인덱스 초기화 실패", e);
    }
  }

  /**
   * 기존 인덱스 삭제
   */
  private void deleteIndexIfExists() {
    try {
      ExistsRequest existsRequest = ExistsRequest.of(e -> e.index(contentIndex));

      if (openSearchClient.indices().exists(existsRequest).value()) {
        openSearchClient.indices().delete(d -> d.index(contentIndex));
        log.info("기존 인덱스 삭제 완료: {}", contentIndex);
      } else {
        log.info("삭제할 인덱스가 존재하지 않음: {}", contentIndex);
      }
    } catch (IOException e) {
      log.warn("인덱스 삭제 실패: {}", contentIndex, e);
      throw new RuntimeException("인덱스 삭제 실패", e);
    }
  }

  /**
   * 모든 콘텐츠를 OpenSearch에 재인덱싱
   */
  private void reindexAllContents() {
    log.info("모든 콘텐츠 재인덱싱 시작...");

    try {
      List<Content> allContents = contentRepository.findAll();
      log.info("DB에서 {}개의 콘텐츠 조회", allContents.size());

      if (allContents.isEmpty()) {
        log.info("재인덱싱할 콘텐츠가 없습니다.");
        return;
      }

      // 배치 크기로 나누어 처리 (메모리 효율성)
      int batchSize = 100;
      int totalBatches = (int) Math.ceil((double) allContents.size() / batchSize);

      for (int i = 0; i < totalBatches; i++) {
        int startIndex = i * batchSize;
        int endIndex = Math.min(startIndex + batchSize, allContents.size());

        List<Content> batch = allContents.subList(startIndex, endIndex);
        batchIndexContents(batch);

        log.info("배치 {}/{} 완료 ({}-{})", i + 1, totalBatches, startIndex + 1, endIndex);
      }

      log.info("모든 콘텐츠 재인덱싱 완료: 총 {}개", allContents.size());

    } catch (Exception e) {
      log.error("콘텐츠 재인덱싱 실패", e);
      throw new RuntimeException("콘텐츠 재인덱싱 실패", e);
    }
  }

  private List<Content> fallbackSearch(String keyword) {
    log.info("DB 폴백 검색 - 키워드: '{}'", keyword);
    return contentRepository.findByKeyword(keyword)
        .stream()
        .limit(50)
        .collect(Collectors.toList());
  }
}
