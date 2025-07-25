package team03.mopl.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class ElasticsearchConfig {

  @Value("${spring.elasticsearch.uris}")
  private String elasticsearchUrl;

  @Bean
  public ElasticsearchClient elasticsearchClient() {
    // RestClient 생성
    RestClient restClient = RestClient.builder(
        HttpHost.create(elasticsearchUrl)
    ).build();

    // Transport 생성 (Jackson JSON 매퍼 사용)
    ElasticsearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper()
    );

    // ElasticsearchClient 생성
    return new ElasticsearchClient(transport);
  }
}
