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

  // 또는 별도의 AWSCredentialsProvider 빈이 있다면 이렇게 사용
  // public OpenSearchRestClientConfiguration(AWSCredentialsProvider credentialsProvider) {
  //     this.credentialsProvider = credentialsProvider;
  // }

  @Bean
  public AWSCredentialsProvider awsCredentialsProvider() {
    return DefaultAWSCredentialsProviderChain.getInstance();
  }

  /**
   * SpringDataOpenSearch data provides us the flexibility to implement our custom {@link RestHighLevelClient} instance
   * by implementing the abstract method {@link AbstractOpenSearchConfiguration#opensearchClient()},
   *
   * @return RestHighLevelClient. Amazon OpenSearch Service Https rest calls have to be signed with AWS credentials,
   * hence an interceptor {@link HttpRequestInterceptor} is required to sign every API calls with credentials.
   * The signing is happening through the below snippet
   * <code>
   * signer.sign(signableRequest, awsCredentialsProvider.getCredentials());
   * </code>
   */
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

    RestClientBuilder restClientBuilder = RestClient.builder(
            new HttpHost(endpoint, 443, "https"))
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
}
