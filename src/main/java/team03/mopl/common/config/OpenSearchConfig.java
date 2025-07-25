package team03.mopl.common.config;

import org.apache.http.HttpHost;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@Profile("dev")
@EnableElasticsearchRepositories(basePackages = "team03.mopl.domain.curation.elasticsearch")
public class OpenSearchConfig extends AbstractOpenSearchConfiguration {

  @Value("${spring.elasticsearch.uris}")
  private String openSearchUrl;

  // Spring Data OpenSearch용 RestHighLevelClient
  @Override
  @Bean
  public RestHighLevelClient opensearchClient() {
    final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
        .connectedTo(openSearchUrl.replace("http://", "").replace("https://", ""))
        .build();

    return RestClients.create(clientConfiguration).rest();
  }

  // 새로운 OpenSearch Java Client용 빈 추가
  @Bean
  public OpenSearchClient openSearchClient() {
    // RestClient 생성
    RestClient restClient = RestClient.builder(
        HttpHost.create(openSearchUrl)
    ).build();

    // Transport 생성 (Jackson JSON 매퍼 사용)
    OpenSearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper()
    );

    // OpenSearchClient 생성
    return new OpenSearchClient(transport);
  }
}
