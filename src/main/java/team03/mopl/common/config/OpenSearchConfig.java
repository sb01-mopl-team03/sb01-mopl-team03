package team03.mopl.common.config;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClients;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;

@Configuration
public class OpenSearchConfig {

  private static final Logger logger = LoggerFactory.getLogger(OpenSearchConfig.class);

  @Value("${opensearch.endpoint}")
  private String endpoint;

  @Value("${opensearch.use-aws-auth:true}")
  private boolean useAwsAuth;

  @Value("${opensearch.region:ap-northeast-2}")
  private String region;

  @Bean
  public OpenSearchClient openSearchClient() {
    logger.info("OpenSearch 클라이언트 설정 시작");
    logger.info("OpenSearch 설정 - endpoint: {}, useAwsAuth: {}, region: {}", endpoint, useAwsAuth, region);

    try {
      // 1. HttpHost 생성
      logger.debug("HttpHost 생성 중...");
      HttpHost host = HttpHost.create(endpoint);
      logger.info("HttpHost 생성 성공 - Host: {}, Port: {}, Scheme: {}",
          host.getHostName(), host.getPort(), host.getSchemeName());

      // 2. RestClient 빌더 생성
      logger.debug("RestClient 빌더 생성 중...");
      RestClientBuilder builder = RestClient.builder(host);

      if (useAwsAuth) {
        logger.info("AWS 인증 설정 시작");

        try {
          // 3. AWS 자격 증명 공급자 생성
          logger.debug("AWS 자격 증명 공급자 생성 중...");
          DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
          logger.debug("AWS 자격 증명 공급자 생성 완료");

          // 4. 자격 증명 테스트
          logger.debug("AWS 자격 증명 테스트 중...");
          try {
            var credentials = credentialsProvider.resolveCredentials();
            logger.info("AWS 자격 증명 확인 성공 - Access Key ID: {}...",
                credentials.accessKeyId().substring(0, Math.min(10, credentials.accessKeyId().length())));
          } catch (Exception e) {
            logger.error("AWS 자격 증명 확인 실패", e);
            throw new RuntimeException("AWS 자격 증명을 확인할 수 없습니다", e);
          }

          // 5. AWS4 서명자 생성
          logger.debug("AWS4 서명자 생성 중...");
          Aws4Signer signer = Aws4Signer.create();
          logger.debug("AWS4 서명자 생성 완료");

          // 6. HTTP 요청 인터셉터 생성
          logger.debug("HTTP 요청 인터셉터 생성 중...");
          AWSRequestSigningApacheInterceptor interceptor =
              new AWSRequestSigningApacheInterceptor("es", signer, credentialsProvider, region);
          logger.debug("HTTP 요청 인터셉터 생성 완료");

          // 7. HTTP 클라이언트 설정
          logger.debug("AWS 인증이 적용된 HTTP 클라이언트 설정 중...");
          builder.setHttpClientConfigCallback(httpClientBuilder -> {
            logger.debug("HttpClient 콜백 실행 - 인터셉터 추가");
            return httpClientBuilder.addInterceptorLast(interceptor);
          });
          logger.info("AWS 인증 설정 완료");

        } catch (Exception e) {
          logger.error("AWS 인증 설정 중 오류 발생", e);
          throw new RuntimeException("AWS 인증 설정에 실패했습니다", e);
        }
      } else {
        logger.info("AWS 인증 미사용 - 기본 HTTP 클라이언트 사용");
      }

      // 8. RestClient 생성
      logger.debug("RestClient 생성 중...");
      RestClient restClient = builder.build();
      logger.debug("RestClient 생성 완료");

      // 9. Transport 생성
      logger.debug("RestClientTransport 생성 중...");
      RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
      logger.debug("RestClientTransport 생성 완료");

      // 10. OpenSearchClient 생성
      logger.debug("OpenSearchClient 생성 중...");
      OpenSearchClient client = new OpenSearchClient(transport);
      logger.info("OpenSearchClient 생성 완료");

      return client;

    } catch (Exception e) {
      logger.error("OpenSearch 클라이언트 생성 실패", e);
      throw new RuntimeException("OpenSearch 클라이언트를 생성할 수 없습니다", e);
    }
  }
}
