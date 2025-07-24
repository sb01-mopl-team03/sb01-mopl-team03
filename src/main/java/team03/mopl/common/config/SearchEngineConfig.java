package team03.mopl.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

// AWS SDK 1.x 방식 import
import com.amazonaws.auth.AWS4Signer;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.AWSCredentialsProvider;

import java.time.Duration;

@Configuration
public class SearchEngineConfig {

  private static final Logger log = LoggerFactory.getLogger(SearchEngineConfig.class);

  @Value("${spring.elasticsearch.uris:http://localhost:9200}")
  private String elasticsearchUrl;

  @Value("${spring.elasticsearch.region:ap-northeast-2}")
  private String awsRegion;

  public SearchEngineConfig() {
    log.info("=== SearchEngineConfig 클래스가 생성되었습니다 ===");
  }

  // ===== 로컬 개발환경용 (Elasticsearch) =====
  @Bean
  @Primary
  @Profile("dev")
  public ElasticsearchClient elasticsearchClient() {
    log.info("=== DEV용 ElasticsearchClient 빈을 생성합니다 ===");

    RestClient restClient = RestClient.builder(HttpHost.create(elasticsearchUrl))
        .build();

    ElasticsearchTransport transport = new RestClientTransport(
        restClient, new JacksonJsonpMapper()
    );

    return new ElasticsearchClient(transport);
  }

  @Bean(name = {"elasticsearchTemplate", "elasticsearchOperations"})
  @Primary
  @Profile("dev")
  public ElasticsearchOperations elasticsearchTemplate() {
    log.info("=== DEV용 elasticsearchTemplate 빈을 생성합니다 ===");
    return new ElasticsearchTemplate(elasticsearchClient());
  }

  // ===== AWS 환경용 (Elasticsearch with IAM 서명) =====
  @Bean
  @Primary
  @Profile("prod")
  public ElasticsearchClient awsElasticsearchClient() {
    log.info("=== PROD용 ElasticsearchClient 빈을 생성합니다 ===");
    log.info("Elasticsearch URL: {}", elasticsearchUrl);
    log.info("AWS Region: {}", awsRegion);

    try {
      // AWS 자격증명 제공자 (ECS Task Role 자동 사용)
      AWSCredentialsProvider credentialsProvider = DefaultAWSCredentialsProviderChain.getInstance();
      log.info("AWS 자격증명 제공자 생성 완료");

      // AWS4 서명자 설정
      AWS4Signer signer = new AWS4Signer();
      signer.setServiceName("es");
      signer.setRegionName(awsRegion);
      log.info("AWS4 서명자 설정 완료 - 서비스: es, 리전: {}", awsRegion);

      // AWS 서명 인터셉터 생성
      HttpRequestInterceptor interceptor = new AWSRequestSigningApacheInterceptor(
          "es", signer, credentialsProvider
      );
      log.info("AWS 서명 인터셉터 생성 완료");

      // URL에서 호스트 정보 추출
      String cleanUrl = elasticsearchUrl.replaceAll("^https?://", "");
      String[] parts = cleanUrl.split(":");
      String hostname = parts[0];
      int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 443;

      log.info("연결 정보 - 호스트: {}, 포트: {}", hostname, port);

      // RestClient 생성 (AWS 서명 포함)
      RestClient restClient = RestClient.builder(new HttpHost(hostname, port, "https"))
          .setHttpClientConfigCallback(httpClientBuilder ->
              httpClientBuilder.addInterceptorLast(interceptor)
          )
          .setRequestConfigCallback(requestConfigBuilder ->
              requestConfigBuilder
                  .setConnectTimeout((int) Duration.ofSeconds(30).toMillis())
                  .setSocketTimeout((int) Duration.ofSeconds(60).toMillis())
          )
          .build();

      log.info("RestClient 생성 완료");

      // ElasticsearchTransport 생성
      ElasticsearchTransport transport = new RestClientTransport(
          restClient, new JacksonJsonpMapper()
      );

      log.info("ElasticsearchTransport 생성 완료");

      ElasticsearchClient client = new ElasticsearchClient(transport);
      log.info("=== PROD용 ElasticsearchClient 빈 생성 완료! ===");

      return client;

    } catch (Exception e) {
      log.error("PROD용 ElasticsearchClient 생성 중 오류 발생: {}", e.getMessage(), e);
      throw new RuntimeException("ElasticsearchClient 생성 실패", e);
    }
  }

  @Bean(name = {"elasticsearchTemplate", "elasticsearchOperations"})
  @Primary
  @Profile("prod")
  public ElasticsearchOperations prodElasticsearchTemplate() {
    log.info("=== PROD용 elasticsearchTemplate 빈을 생성합니다 ===");
    return new ElasticsearchTemplate(awsElasticsearchClient());
  }
}
