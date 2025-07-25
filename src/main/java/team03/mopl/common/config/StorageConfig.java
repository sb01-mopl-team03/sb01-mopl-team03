package team03.mopl.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Component
public class StorageConfig {

  @Bean
  @ConditionalOnProperty(name = "mopl.storage.type",havingValue = "s3")
  public S3Client s3Client(
      @Value("${mopl.storage.s3.access-key}") String accessKey,
      @Value("${mopl.storage.s3.secret-key}") String secretKey,
      @Value("${mopl.storage.s3.region}") String region
  ) {
    return S3Client.builder()
        .region(Region.of(region))
        .credentialsProvider(StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey,secretKey)
        ))
        .build();
  }
}
