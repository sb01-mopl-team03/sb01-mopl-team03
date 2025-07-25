package team03.mopl.common.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;

import java.net.URI;

@Slf4j
@Configuration
public class OpenSearchConfig {

  @Value("${AWS_S3_REGION}")
  private String region;

  @Value("${AWS_S3_ACCESS_KEY}")
  private String accessKey;

  @Value("${AWS_S3_SECRET_KEY}")
  private String secretKey;

  @Value("${opensearch.endpoint}")
  private String endpoint;

  @Value("${opensearch.use-aws-auth}")
  private boolean useAwsAuth;

  @PostConstruct
  public void logConfiguration() {
    log.info("OpenSearch 설정 - endpoint: {}, useAwsAuth: {}", endpoint, useAwsAuth);
  }

  @Bean
  public OpenSearchClient openSearchClient() {
    // URI 파싱하여 HttpHost 생성
    URI uri = URI.create(ensureProtocol(endpoint));
    HttpHost httpHost = new HttpHost(
        uri.getHost(),
        uri.getPort() != -1 ? uri.getPort() : (uri.getScheme().equals("https") ? 443 : 80),
        uri.getScheme()
    );

    RestClient restClient;

    // AWS 인증이 필요한 경우에만 인터셉터 추가
    if (useAwsAuth) {
      AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
      AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
      Aws4Signer signer = Aws4Signer.create();

      HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
          "es", // service name
          signer,
          credentialsProvider,
          region
      );

      restClient = RestClient.builder(httpHost)
          .setHttpClientConfigCallback(httpClientBuilder ->
              httpClientBuilder.addInterceptorLast(interceptor))
          .build();
    } else {
      // 로컬 환경에서는 인증 없이 생성
      restClient = RestClient.builder(httpHost).build();
    }

    RestClientTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper());

    return new OpenSearchClient(transport);
  }

  private String ensureProtocol(String endpoint) {
    if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
      if (useAwsAuth) {
        return "https://" + endpoint;
      } else {
        return "http://" + endpoint;
      }
    }
    return endpoint;
  }
}
