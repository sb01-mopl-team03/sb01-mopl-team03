package team03.mopl.domain.curation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.ErrorCause;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.cluster.OpenSearchClusterClient;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.IndexResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.bulk.BulkResponseItem;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.opensearch.core.search.HitsMetadata;
import org.opensearch.client.opensearch.core.search.TotalHits;
import org.opensearch.client.opensearch.core.search.TotalHitsRelation;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.CreateIndexResponse;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.opensearch.client.opensearch.indices.OpenSearchIndicesClient;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.entity.ContentSearch;
import team03.mopl.domain.curation.entity.ContentSearchResult;
import team03.mopl.domain.curation.service.ContentSearchService;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Nested;
import org.opensearch.client.opensearch.cluster.HealthResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.endpoints.BooleanResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("컨텐츠 검색 서비스 테스트")
class ContentSearchServiceTest {

  @Mock
  private OpenSearchClient openSearchClient;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private OpenSearchIndicesClient indicesClient;

  @Mock
  private OpenSearchClusterClient clusterClient;

  @InjectMocks
  private ContentSearchService contentSearchService;

  private Content testContent;
  private Content testContent2;
  private UUID contentId;
  private UUID contentId2;

  @BeforeEach
  void setUp() {
    // 테스트 데이터 초기화
    contentId = UUID.randomUUID();
    contentId2 = UUID.randomUUID();

    testContent = Content.builder()
        .id(contentId)
        .title("액션 영화")
        .description("액션과 모험이 가득한 영화")
        .contentType(ContentType.MOVIE)
        .releaseDate(LocalDateTime.now())
        .avgRating(BigDecimal.valueOf(4.5))
        .build();

    testContent2 = Content.builder()
        .id(contentId2)
        .title("로맨틱 코미디")
        .description("따뜻한 로맨스와 유머가 있는 영화")
        .contentType(ContentType.MOVIE)
        .releaseDate(LocalDateTime.now())
        .avgRating(BigDecimal.valueOf(4.0))
        .build();

    // Mock 설정 (모든 기본 설정을 lenient로)
    lenient().when(openSearchClient.indices()).thenReturn(indicesClient);
    lenient().when(openSearchClient.cluster()).thenReturn(clusterClient);
  }

  @Nested
  @DisplayName("인덱스 초기화 테스트")
  class InitializeIndexTest {

    @Test
    @DisplayName("OpenSearch 연결 성공 시 인덱스 초기화")
    void initializeIndex_SuccessfulConnection() throws IOException {
      // given
      HealthResponse healthResponse = mock(HealthResponse.class);
      CreateIndexResponse createResponse = mock(CreateIndexResponse.class);
      BooleanResponse existsResponse = mock(BooleanResponse.class);

      lenient().when(clusterClient.health()).thenReturn(healthResponse);
      lenient().when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);
      lenient().when(existsResponse.value()).thenReturn(false);
      lenient().when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createResponse);
      lenient().when(createResponse.acknowledged()).thenReturn(true);
      lenient().when(contentRepository.findAll()).thenReturn(List.of());

      // when
      assertDoesNotThrow(() -> contentSearchService.initializeIndex());

      // then
      verify(clusterClient).health();
      verify(indicesClient, atLeastOnce()).exists(any(ExistsRequest.class));
    }

    @Test
    @DisplayName("OpenSearch 연결 실패 시 인덱스 초기화 건너뛰기")
    void initializeIndex_ConnectionFailure() throws IOException {
      // given
      lenient().when(clusterClient.health()).thenThrow(new IOException("Connection failed"));

      // when
      assertDoesNotThrow(() -> contentSearchService.initializeIndex());

      // then
      verify(clusterClient).health();
    }

    @Test
    @DisplayName("인덱스가 이미 존재하고 문서가 있는 경우")
    void initializeIndex_IndexExistsWithDocuments() throws IOException {
      // given
      HealthResponse healthResponse = mock(HealthResponse.class);
      SearchResponse<ContentSearch> searchResponse = createMockSearchResponse(5L, List.of());
      BooleanResponse existsResponse = mock(BooleanResponse.class);

      lenient().when(clusterClient.health()).thenReturn(healthResponse);
      lenient().when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);
      lenient().when(existsResponse.value()).thenReturn(true);
      lenient().when(openSearchClient.search(any(SearchRequest.class), eq(ContentSearch.class)))
          .thenReturn(searchResponse);

      // when
      contentSearchService.initializeIndex();

      // then
      verify(clusterClient).health();
    }

    @Test
    @DisplayName("인덱스는 존재하지만 문서가 없는 경우 재인덱싱")
    void initializeIndex_IndexExistsButNoDocuments() throws IOException {
      // given
      HealthResponse healthResponse = mock(HealthResponse.class);
      SearchResponse<ContentSearch> searchResponse = createMockSearchResponse(0L, List.of());
      BooleanResponse existsResponse = mock(BooleanResponse.class);

      lenient().when(clusterClient.health()).thenReturn(healthResponse);
      lenient().when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);
      lenient().when(existsResponse.value()).thenReturn(true);
      lenient().when(openSearchClient.search(any(SearchRequest.class), eq(ContentSearch.class)))
          .thenReturn(searchResponse);
      lenient().when(contentRepository.findAll()).thenReturn(List.of(testContent));

      // when
      contentSearchService.initializeIndex();

      // then (간단한 검증만)
      verify(clusterClient).health();
    }
  }

  @Nested
  @DisplayName("단일 콘텐츠 인덱싱 테스트")
  class IndexContentTest {

    @Test
    @DisplayName("성공적인 콘텐츠 인덱싱")
    void indexContent_Success() throws IOException {
      // given
      IndexResponse indexResponse = mock(IndexResponse.class);
      lenient().when(indexResponse.id()).thenReturn(contentId.toString());
      lenient().when(indexResponse.result()).thenReturn(Result.Created);

      lenient().when(openSearchClient.index(any(IndexRequest.class))).thenReturn(indexResponse);

      // when
      assertDoesNotThrow(() -> contentSearchService.indexContent(testContent));

      // then
      verify(openSearchClient).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("인덱싱 실패 시 RuntimeException 발생")
    void indexContent_IOException() throws IOException {
      // given
      lenient().when(openSearchClient.index(any(IndexRequest.class)))
          .thenThrow(new IOException("Indexing failed"));

      // when & then
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        contentSearchService.indexContent(testContent);
      });

      assertEquals("콘텐츠 인덱싱 실패", exception.getMessage());
      verify(openSearchClient).index(any(IndexRequest.class));
    }
  }

  @Nested
  @DisplayName("배치 인덱싱 테스트")
  class BatchIndexContentsTest {

    @Test
    @DisplayName("성공적인 배치 인덱싱")
    void batchIndexContents_Success() throws IOException {
      // given
      List<Content> contents = List.of(testContent, testContent2);
      BulkResponse bulkResponse = createSuccessfulBulkResponse();

      lenient().when(openSearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

      // when
      assertDoesNotThrow(() -> contentSearchService.batchIndexContents(contents));

      // then
      verify(openSearchClient).bulk(any(BulkRequest.class));
    }

    @Test
    @DisplayName("빈 콘텐츠 목록으로 배치 인덱싱")
    void batchIndexContents_EmptyList() {
      // when
      contentSearchService.batchIndexContents(List.of());

      // then
      verifyNoInteractions(openSearchClient);
    }

    @Test
    @DisplayName("null 콘텐츠 목록으로 배치 인덱싱")
    void batchIndexContents_NullList() {
      // when
      contentSearchService.batchIndexContents(null);

      // then
      verifyNoInteractions(openSearchClient);
    }

    @Test
    @DisplayName("배치 인덱싱 일부 실패")
    void batchIndexContents_PartialFailure() throws IOException {
      // given
      List<Content> contents = List.of(testContent, testContent2);
      BulkResponse bulkResponse = createPartialFailureBulkResponse();

      lenient().when(openSearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

      // when
      contentSearchService.batchIndexContents(contents);

      // then
      verify(openSearchClient).bulk(any(BulkRequest.class));
    }

    @Test
    @DisplayName("배치 인덱싱 완전 실패 시 RuntimeException")
    void batchIndexContents_CompleteFailure() throws IOException {
      // given
      List<Content> contents = List.of(testContent);
      lenient().when(openSearchClient.bulk(any(BulkRequest.class)))
          .thenThrow(new IOException("Bulk operation failed"));

      // when & then
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        contentSearchService.batchIndexContents(contents);
      });

      assertEquals("배치 인덱싱 실패", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("콘텐츠 검색 테스트")
  class FindContentsByKeywordTest {

    @Test
    @DisplayName("성공적인 키워드 검색")
    void findContentsByKeywordWithScore_Success() throws IOException {
      // given
      String keyword = "액션";
      List<Hit<ContentSearch>> hits = List.of(
          createMockHit(contentId.toString(), 8.5),
          createMockHit(contentId2.toString(), 7.0)
      );

      // 첫 번째 호출용 (checkIndexStatusForSearch에서 문서 수 확인)
      SearchResponse<ContentSearch> countResponse = createMockSearchResponse(10L, List.of());

      // 두 번째 호출용 (실제 검색)
      SearchResponse<ContentSearch> searchResponse = createMockSearchResponse(2L, hits);

      BooleanResponse existsResponse = mock(BooleanResponse.class);

      when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);
      when(existsResponse.value()).thenReturn(true);

      // 두 번의 search 호출에 대해 순차적으로 다른 응답 설정
      when(openSearchClient.search(any(SearchRequest.class), eq(ContentSearch.class)))
          .thenReturn(countResponse)    // 첫 번째: 문서 수 확인
          .thenReturn(searchResponse);  // 두 번째: 실제 검색

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(testContent));
      when(contentRepository.findById(contentId2)).thenReturn(Optional.of(testContent2));

      // when
      List<ContentSearchResult> results = contentSearchService.findContentsByKeywordWithScore(keyword);

      // then
      assertNotNull(results);
      assertEquals(2, results.size());
      assertEquals(testContent.getId(), results.get(0).getContent().getId());
      assertEquals(8.5, results.get(0).getScore());
      assertEquals(testContent2.getId(), results.get(1).getContent().getId());
      assertEquals(7.0, results.get(1).getScore());

      // 두 번 호출되는 것이 정상이므로 times(2)로 변경
      verify(openSearchClient, times(2)).search(any(SearchRequest.class), eq(ContentSearch.class));
    }

    @Test
    @DisplayName("검색 결과가 없는 경우")
    void findContentsByKeywordWithScore_NoResults() throws IOException {
      // given
      String keyword = "존재하지않는키워드";
      SearchResponse<ContentSearch> searchResponse = createMockSearchResponse(0L, List.of());

      BooleanResponse existsResponse = mock(BooleanResponse.class);

      when(indicesClient.exists(isA(ExistsRequest.class))).thenReturn(existsResponse);
      when(existsResponse.value()).thenReturn(true);
      when(openSearchClient.search(any(SearchRequest.class), eq(ContentSearch.class)))
          .thenReturn(searchResponse);

      // when
      List<ContentSearchResult> results = contentSearchService.findContentsByKeywordWithScore(keyword);

      // then
      assertNotNull(results);
      assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("검색 중 IOException 발생 시 빈 목록 반환")
    void findContentsByKeywordWithScore_IOException() throws IOException {
      // given
      String keyword = "액션";
      BooleanResponse existsResponse = mock(BooleanResponse.class);

      lenient().when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);
      lenient().when(existsResponse.value()).thenReturn(false); // 인덱스 상태 확인 건너뛰기
      lenient().when(openSearchClient.search(any(SearchRequest.class), eq(ContentSearch.class)))
          .thenThrow(new IOException("Search failed"));

      // when
      List<ContentSearchResult> results = contentSearchService.findContentsByKeywordWithScore(keyword);

      // then
      assertNotNull(results);
      assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("페이징을 포함한 키워드 검색")
    void findContentsByKeywordWithScore_WithPaging() throws IOException {
      // given
      String keyword = "액션";
      int size = 10;
      int from = 5;

      // 첫 번째 호출용 (checkIndexStatusForSearch에서 문서 수 확인)
      SearchResponse<ContentSearch> countResponse = createMockSearchResponse(5L, List.of());

      // 두 번째 호출용 (실제 검색)
      SearchResponse<ContentSearch> searchResponse = createMockSearchResponse(1L,
          List.of(createMockHit(contentId.toString(), 8.5)));

      BooleanResponse existsResponse = mock(BooleanResponse.class);

      when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);
      when(existsResponse.value()).thenReturn(true);

      // 두 번의 search 호출에 대해 순차적으로 다른 응답 설정
      when(openSearchClient.search(any(SearchRequest.class), eq(ContentSearch.class)))
          .thenReturn(countResponse)    // 첫 번째: 문서 수 확인
          .thenReturn(searchResponse);  // 두 번째: 실제 검색

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(testContent));

      // when
      List<ContentSearchResult> results = contentSearchService.findContentsByKeywordWithScore(keyword, size, from);

      // then
      assertNotNull(results);
      assertEquals(1, results.size());
      // 두 번 호출되는 것이 정상이므로 times(2)로 변경
      verify(openSearchClient, times(2)).search(any(SearchRequest.class), eq(ContentSearch.class));
    }

    @Test
    @DisplayName("검색된 콘텐츠가 DB에 존재하지 않는 경우")
    void findContentsByKeywordWithScore_ContentNotFoundInDB() throws IOException {
      // given
      String keyword = "액션";
      List<Hit<ContentSearch>> hits = List.of(createMockHit(contentId.toString(), 8.5));
      SearchResponse<ContentSearch> searchResponse = createMockSearchResponse(1L, hits);

      BooleanResponse existsResponse = mock(BooleanResponse.class);
      when(indicesClient.exists(isA(ExistsRequest.class))).thenReturn(existsResponse);
      when(openSearchClient.search(any(SearchRequest.class), eq(ContentSearch.class)))
          .thenReturn(searchResponse);
      when(contentRepository.findById(contentId)).thenReturn(Optional.empty());

      // when
      List<ContentSearchResult> results = contentSearchService.findContentsByKeywordWithScore(keyword);

      // then
      assertNotNull(results);
      assertTrue(results.isEmpty());
    }
  }

  @Nested
  @DisplayName("인덱스 관리 테스트")
  class IndexManagementTest {

    @Test
    @DisplayName("성공적인 인덱스 초기화")
    void initializeIndexWithAllContents_Success() throws IOException {
      // given
      DeleteIndexResponse deleteResponse = mock(DeleteIndexResponse.class);
      CreateIndexResponse createResponse = mock(CreateIndexResponse.class);

      // 핵심: BulkResponse 모킹 추가
      BulkResponse bulkResponse = createSuccessfulBulkResponse();

      // deleteIndexIfExists()에서의 exists 호출 - 인덱스가 존재한다고 설정
      BooleanResponse deleteExistsResponse = mock(BooleanResponse.class);
      when(deleteExistsResponse.value()).thenReturn(true);

      // createIndexIfNotExists()에서의 exists 호출 - 인덱스가 존재하지 않는다고 설정
      BooleanResponse createExistsResponse = mock(BooleanResponse.class);
      when(createExistsResponse.value()).thenReturn(false);

      // exists 호출 순서대로 설정
      when(indicesClient.exists(any(ExistsRequest.class)))
          .thenReturn(deleteExistsResponse) // deleteIndexIfExists에서 사용
          .thenReturn(createExistsResponse); // createIndexIfNotExists에서 사용

      // delete와 create 응답 설정
      when(indicesClient.delete(any(Function.class))).thenReturn(deleteResponse);
      when(deleteResponse.acknowledged()).thenReturn(true);

      // 중요: openSearchClient.indices().create() 호출을 모킹
      // 서비스에서 openSearchClient.indices().create()를 직접 호출하므로 이것도 모킹해야 함
      when(openSearchClient.indices()).thenReturn(indicesClient);
      when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createResponse);
      when(createResponse.acknowledged()).thenReturn(true);

      // 데이터와 bulk 응답 설정
      when(contentRepository.findAll()).thenReturn(List.of(testContent, testContent2));
      when(openSearchClient.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

      // when
      assertDoesNotThrow(() -> contentSearchService.initializeIndexWithAllContents());

      // then
      verify(indicesClient).delete(any(Function.class));
      verify(indicesClient).create(any(CreateIndexRequest.class));
      verify(contentRepository).findAll();
      verify(openSearchClient).bulk(any(BulkRequest.class));
    }
  }

  // Helper methods for creating mock objects
  private SearchResponse<ContentSearch> createMockSearchResponse(long totalHits, List<Hit<ContentSearch>> hits) {
    SearchResponse<ContentSearch> searchResponse = mock(SearchResponse.class);
    HitsMetadata<ContentSearch> hitsMetadata = mock(HitsMetadata.class);
    TotalHits total = mock(TotalHits.class);

    lenient().when(total.value()).thenReturn(totalHits);
    lenient().when(total.relation()).thenReturn(TotalHitsRelation.Eq);
    lenient().when(hitsMetadata.total()).thenReturn(total);
    lenient().when(hitsMetadata.hits()).thenReturn(hits);
    lenient().when(searchResponse.hits()).thenReturn(hitsMetadata);

    return searchResponse;
  }

  private Hit<ContentSearch> createMockHit(String id, double score) {
    Hit<ContentSearch> hit = mock(Hit.class);
    lenient().when(hit.id()).thenReturn(id);
    lenient().when(hit.score()).thenReturn(score);
    return hit;
  }

  private BulkResponse createSuccessfulBulkResponse() {
    BulkResponse bulkResponse = mock(BulkResponse.class);
    BulkResponseItem item1 = mock(BulkResponseItem.class);
    BulkResponseItem item2 = mock(BulkResponseItem.class);

    when(item1.error()).thenReturn(null);
    when(item2.error()).thenReturn(null);
    when(bulkResponse.errors()).thenReturn(false);
    when(bulkResponse.items()).thenReturn(List.of(item1, item2));

    return bulkResponse;
  }

  private BulkResponse createPartialFailureBulkResponse() {
    BulkResponse bulkResponse = mock(BulkResponse.class);
    BulkResponseItem successItem = mock(BulkResponseItem.class);
    BulkResponseItem failureItem = mock(BulkResponseItem.class);
    ErrorCause errorCause = mock(ErrorCause.class);

    when(successItem.error()).thenReturn(null);
    when(failureItem.error()).thenReturn(errorCause);
    when(failureItem.id()).thenReturn(contentId2.toString());
    when(errorCause.reason()).thenReturn("Indexing failed");

    when(bulkResponse.errors()).thenReturn(true);
    when(bulkResponse.items()).thenReturn(List.of(successItem, failureItem));

    return bulkResponse;
  }
}
