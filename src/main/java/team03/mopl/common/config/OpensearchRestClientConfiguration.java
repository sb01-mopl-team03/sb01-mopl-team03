package team03.mopl.common.config;

import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.client.RestClient;
import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Slf4j
@Profile("prod")
public class OpensearchRestClientConfiguration {

  @Value("${spring.elasticsearch.uris}")
  private String endpoint;

  @Value("${spring.elasticsearch.region:ap-northeast-2}")
  private String region;

  @Bean
  public OpenSearchClient openSearchClient() {
    log.info("Creating OpenSearchClient bean for endpoint: {}", endpoint);

    try {
      // endpoint에서 호스트와 포트 추출
      String cleanEndpoint = endpoint.replace("https://", "").replace("http://", "");
      String[] parts = cleanEndpoint.split(":");
      String host = parts[0];
      int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 443;

      // RestClient 생성
      RestClient restClient = RestClient.builder(
              new HttpHost(host, port, "https")
          )
          .setRequestConfigCallback(requestConfigBuilder ->
              requestConfigBuilder
                  .setConnectTimeout(10000)
                  .setSocketTimeout(60000))
          .build();

      // Transport 생성
      RestClientTransport transport = new RestClientTransport(
          restClient,
          new JacksonJsonpMapper()
      );

      // OpenSearchClient 생성
      return new OpenSearchClient(transport);

    } catch (Exception e) {
      log.error("Failed to create OpenSearch client", e);
      throw new RuntimeException("Failed to create OpenSearch client", e);
    }
  }
}
