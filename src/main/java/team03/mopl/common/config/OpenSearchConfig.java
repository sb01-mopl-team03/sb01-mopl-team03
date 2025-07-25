package team03.mopl.common.config;

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

@Configuration
public class OpenSearchConfig {

  @Value("${AWS_S3_REGION}")
  private String region;

  @Value("${AWS_S3_ACCESS_KEY}")
  private String accessKey;

  @Value("${AWS_S3_SECRET_KEY}")
  private String secretKey;

  @Value("${OPENSEARCH_ENDPOINT}")
  private String endpoint;

  @Bean
  public OpenSearchClient openSearchClient() {
    AwsCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
    AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

    Aws4Signer signer = Aws4Signer.create();

    HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
        "es", // service name
        signer,
        credentialsProvider,
        region
    );

    // URI 파싱하여 HttpHost 생성
    URI uri = URI.create(ensureProtocol(endpoint));
    HttpHost httpHost = new HttpHost(
        uri.getHost(),
        uri.getPort() != -1 ? uri.getPort() : (uri.getScheme().equals("https") ? 443 : 80),
        uri.getScheme()
    );

    RestClient restClient = RestClient.builder(httpHost)
        .setHttpClientConfigCallback(httpClientBuilder ->
            httpClientBuilder.addInterceptorLast(interceptor))
        .build();

    RestClientTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper());

    return new OpenSearchClient(transport);
  }

  private String ensureProtocol(String endpoint) {
    if (!endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
      // AWS OpenSearch는 기본적으로 HTTPS 사용
      return "https://" + endpoint;
    }
    return endpoint;
  }
}
