package team03.mopl.common.config;

import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

@Configuration
@EnableElasticsearchRepositories(basePackages = "team03.mopl.domain.curation.elasticsearch")
@Slf4j
@Profile("prod")
public class OpensearchRestClientConfiguration extends AbstractOpenSearchConfiguration {

  @Value("${spring.elasticsearch.uris}")
  private String endpoint;

  @Value("${spring.elasticsearch.region}")
  private String region;

  private final AWSCredentialsProvider credentialsProvider;

  public OpensearchRestClientConfiguration() {
    this.credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
  }

  // 현재 코드는 대부분 유지하되, endpoint 파싱 수정 필요
  @Override
  @Bean
  public RestHighLevelClient opensearchClient() {
    log.info("Creating OpenSearch RestHighLevelClient for endpoint: {}, region: {}", endpoint, region);

    AWS4Signer signer = new AWS4Signer();
    String serviceName = "es";
    signer.setServiceName(serviceName);
    signer.setRegionName(region);

    HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
        serviceName, signer, credentialsProvider);

    // endpoint에서 호스트명만 추출
    String hostName = endpoint.replace("https://", "").replace("http://", "");

    RestClientBuilder restClientBuilder = RestClient.builder(
            new HttpHost(hostName, 443, "https"))
        .setHttpClientConfigCallback(httpClientBuilder ->
            httpClientBuilder.addInterceptorLast(interceptor))
        .setRequestConfigCallback(requestConfigBuilder ->
            requestConfigBuilder
                .setConnectTimeout((int) Duration.ofSeconds(300).toMillis())
                .setSocketTimeout((int) Duration.ofSeconds(150).toMillis()));

    RestHighLevelClient client = new RestHighLevelClient(restClientBuilder);
    log.info("OpenSearch RestHighLevelClient created successfully");
    return client;
  }

  // OpenSearchClient 빈도 추가 필요
  @Bean
  public OpenSearchClient openSearchClient() {
    String hostName = endpoint.replace("https://", "").replace("http://", "");

    AWS4Signer signer = new AWS4Signer();
    signer.setServiceName("es");
    signer.setRegionName(region);

    HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
        "es", signer, credentialsProvider);

    RestClient restClient = RestClient.builder(new HttpHost(hostName, 443, "https"))
        .setHttpClientConfigCallback(httpClientBuilder ->
            httpClientBuilder.addInterceptorLast(interceptor))
        .build();

    OpenSearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper());

    return new OpenSearchClient(transport);
  }
}
