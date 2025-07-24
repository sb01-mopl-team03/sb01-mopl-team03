package team03.mopl.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;

@Configuration
public class SearchEngineConfig {

  @Value("${spring.elasticsearch.uris:http://localhost:9200}")
  private String elasticsearchUrl;

  @Value("${spring.elasticsearch.region:ap-northeast-2}")
  private String awsRegion;

  // ===== 로컬 개발환경용 (Elasticsearch) =====
  @Bean
  @Profile("dev")
  public ElasticsearchClient localElasticsearchClient() {
    RestClient restClient = RestClient.builder(HttpHost.create(elasticsearchUrl))
        .build();

    ElasticsearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper()
    );

    return new ElasticsearchClient(transport);
  }

  // ===== AWS 환경용 (OpenSearch with IAM) =====
  @Bean
  @Profile("prod")
  public OpenSearchClient awsOpenSearchClient() {
    SdkHttpClient httpClient = UrlConnectionHttpClient.builder().build();

    AwsSdk2Transport transport = new AwsSdk2Transport(
        httpClient,
        extractHostFromUrl(elasticsearchUrl),
        "es",
        Region.of(awsRegion),
        AwsSdk2TransportOptions.builder()
            .setCredentials(DefaultCredentialsProvider.create())
            .build()
    );

    return new OpenSearchClient(transport);
  }

  private String extractHostFromUrl(String url) {
    return url.replaceAll("^https?://", "").replaceAll("/$", "");
  }
}
