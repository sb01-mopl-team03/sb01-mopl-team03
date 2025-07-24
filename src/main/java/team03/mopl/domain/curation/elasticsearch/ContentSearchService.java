package team03.mopl.domain.curation.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.DeleteResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSearchService {

  private final ElasticsearchClient elasticsearchClient;
  private final ContentSearchRepository contentSearchRepository;
  private final ContentRepository contentRepository;

  public List<Content> findContentsByKeyword(String keyword) {
    try {
      Query query = Query.of(q -> q
          .bool(b -> b
              // 1. 정확한 매치 (가장 높은 점수)
              .should(s -> s.match(m -> m
                  .field("title")
                  .query(keyword)
                  .boost(8.0f))) // 제목 정확한 매치
              .should(s -> s.match(m -> m
                  .field("description")
                  .query(keyword)
                  .boost(4.5f))) // 설명 정확한 매치

              // 2. 구문 매치
              .should(s -> s.matchPhrase(mp -> mp
                  .field("title")
                  .query(keyword)
                  .boost(6.0f))) // 구문 매치

              // 3. 와일드카드 매치 (부분 문자열)
              .should(s -> s.wildcard(w -> w
                  .field("title")
                  .value("*" + keyword + "*")
                  .boost(7.0f))) // 제목 부분 매치 - 높은 가중치
              .should(s -> s.wildcard(w -> w
                  .field("description")
                  .value("*" + keyword + "*")
                  .boost(2.5f))) // 설명 부분 매치

              // 4. 퍼지 검색 (오타 허용)
              .should(s -> s.fuzzy(f -> f
                  .field("title")
                  .value(keyword)
                  .fuzziness("AUTO")
                  .boost(2.0f)))
              .should(s -> s.fuzzy(f -> f
                  .field("description")
                  .value(keyword)
                  .fuzziness("AUTO")
                  .boost(1.8f)))

              .minimumShouldMatch("1")
          )
      );

      SearchRequest searchRequest = SearchRequest.of(s -> s
          .index("contents_search")
          .query(query)
          .size(50)
          .sort(sort -> sort.score(sc -> sc.order(SortOrder.Desc)))
      );

      SearchResponse<ContentSearch> response = elasticsearchClient.search(searchRequest,
          ContentSearch.class);

      // ContentSearch에서 Content ID만 추출해서 DB에서 조회
      List<String> contentIds = response.hits().hits().stream()
          .map(hit -> hit.source().getId())
          .collect(Collectors.toList());

      // 스코어 정보 로깅
      log.info("=== 검색 결과 상세 ===");
      log.info("키워드: '{}', 총 결과: {}개", keyword, response.hits().total().value());

      response.hits().hits().forEach(hit -> {
        ContentSearch content = hit.source();
        log.info("스코어: {}, 제목: '{}', 설명: '{}'",
            hit.score(),
            content.getTitle(),
            content.getDescription().substring(0, Math.min(50, content.getDescription().length()))
                + "..."
        );
      });

      // DB에서 실제 Content 엔티티들 조회 (스코어 순서 유지)
      List<UUID> uuids = contentIds.stream()
          .map(UUID::fromString)
          .collect(Collectors.toList());

      Map<UUID, Content> contentMap = contentRepository.findAllById(uuids)
          .stream()
          .collect(Collectors.toMap(Content::getId, content -> content));

      // 스코어 순서대로 정렬된 결과 반환
      List<Content> results = uuids.stream()
          .map(contentMap::get)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      log.info("키워드 '{}' 최종 검색 결과: {}개", keyword, results.size());
      return results;

    } catch (IOException e) {
      log.error("Elasticsearch 검색 실패, 폴백 검색 사용: {}", e.getMessage());
      return fallbackSearch(keyword);
    }
  }

    private List<Content> fallbackSearch(String keyword) {
    log.info("DB 폴백 검색 - 키워드: '{}'", keyword);
    return contentRepository.findByKeyword(keyword)
        .stream()
        .limit(50)
        .collect(Collectors.toList());
  }

  public void indexContent(Content content) {
    try {
      ContentSearch searchContent = ContentSearch.from(content);
      contentSearchRepository.save(searchContent);
      log.info("Elasticsearch 인덱싱 성공 - ID: {}, 제목: '{}'", content.getId(), content.getTitle());
    } catch (Exception e) {
      log.error("Elasticsearch 인덱싱 실패 - ID: {}, 제목: '{}', 오류: {}",
          content.getId(), content.getTitle(), e.getMessage(), e);
      // 예외 처리 로직 추가 (예: 재시도, 실패 알림 등)
    }
  }

  public void deleteContent(UUID contentId) {
    try {
      contentSearchRepository.deleteById(contentId.toString()); // String ID로 삭제
      log.info("Elasticsearch 문서 삭제 성공: ID '{}'", contentId);
    } catch (Exception e) {
      log.error("Elasticsearch 문서 삭제 실패 - ID: '{}', 오류: {}",
          contentId, e.getMessage(), e);
      // TODO: 예외 처리 로직 추가
    }
  }
}
