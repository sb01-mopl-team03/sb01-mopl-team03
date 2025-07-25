package team03.mopl.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.GetIndexResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenSearchHealthCheck implements CommandLineRunner {

  private final OpenSearchClient openSearchClient;

  @Override
  public void run(String... args) throws Exception {
    try {
      // OpenSearch에 존재하는 인덱스 목록 중 하나 조회 시도
      GetIndexResponse response = openSearchClient.indices().get(i -> i.index("*"));

      log.info("✅ OpenSearch 연결 성공! 인덱스 목록:");
      response.result().keySet().forEach(index -> log.info(" - {}", index));

    } catch (Exception e) {
      log.error("❌ OpenSearch 연결 실패: {}", e.getMessage(), e);
    }
  }
}
