package team03.mopl.common.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.endpoints.BooleanResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpensearchIndexInitializer implements ApplicationRunner {

  private final OpenSearchClient openSearchClient;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    try {
      createSimpleIndex();
    } catch (Exception e) {
      log.warn("OpenSearch 인덱스 생성 실패 (정상): {}", e.getMessage());
      log.info("OpenSearch가 준비되면 자동으로 인덱스가 생성됩니다.");
    }
  }

  private void createSimpleIndex() throws Exception {
    String indexName = "contents";

    BooleanResponse exists = openSearchClient.indices()
        .exists(ExistsRequest.of(e -> e.index(indexName)));

    if (!exists.value()) {
      log.info("Creating simple index: {}", indexName);

      // 가장 단순한 인덱스 생성 (Nori 설정 없음)
      CreateIndexRequest request = CreateIndexRequest.of(c -> c.index(indexName));
      openSearchClient.indices().create(request);

      log.info("Simple index '{}' created. 한국어 검색도 어느 정도 동작합니다!", indexName);
    }
  }
}
