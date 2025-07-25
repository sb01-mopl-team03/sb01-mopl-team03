package team03.mopl.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class StorageConfig {

  @Bean
  @ConditionalOnProperty(name = "mopl.storage.type", havingValue = "s3")
  public S3Client s3Client(
      @Value("${mopl.storage.s3.access-key:}") String accessKey,
      @Value("${mopl.storage.s3.secret-key:}") String secretKey,
      @Value("${mopl.storage.s3.region:ap-northeast-2}") String region
  ) {
    try {
      // 자격증명이 비어있는 경우 처리
      if (accessKey == null || accessKey.trim().isEmpty() ||
          secretKey == null || secretKey.trim().isEmpty()) {

        // 기본 자격증명 사용 (AWS CLI, IAM Role 등)
        return S3Client.builder()
            .region(Region.of(region))
            .build();
      }

      // 명시적 자격증명 생성
      AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
      AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

      return S3Client.builder()
          .region(Region.of(region))
          .httpClientBuilder(UrlConnectionHttpClient.builder()) // UrlConnectionHttpClient를 명시적으로 사용
          .build();
    } catch (Exception e) {
      System.err.println("Failed to create S3Client: " + e.getMessage());
      e.printStackTrace();

      // 실패 시 기본 설정으로 재시도
      return S3Client.builder()
          .region(Region.of(region))
          .httpClientBuilder(UrlConnectionHttpClient.builder()) // 실패 시 재시도 시에도 명시적으로 사용
          .build();
    }
  }
}
