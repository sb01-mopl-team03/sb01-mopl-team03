package team03.mopl.common.config;

import org.apache.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Profile("dev")
@Configuration
public class OpenSearchConfig {

  @Value("${spring.elasticsearch.uris}")
  private String openSearchUrl;

  // OpenSearch Java Client용 빈 - dev 프로필용
  @Bean
  public OpenSearchClient openSearchClient() {
    try {
      // RestClient 생성
      RestClient restClient = RestClient.builder(
              HttpHost.create(openSearchUrl)
          )
          .setRequestConfigCallback(requestConfigBuilder ->
              requestConfigBuilder
                  .setConnectTimeout(10000)
                  .setSocketTimeout(60000))
          .build();

      // Transport 생성 (Jackson JSON 매퍼 사용)
      RestClientTransport transport = new RestClientTransport(
          restClient, new JacksonJsonpMapper()
      );

      // OpenSearchClient 생성
      return new OpenSearchClient(transport);

    } catch (Exception e) {
      throw new RuntimeException("Failed to create OpenSearch client for dev profile", e);
    }
  }
}
