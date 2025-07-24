//package team03.mopl.common.config;
//
//import org.opensearch.client.opensearch.OpenSearchClient;
//import org.opensearch.client.transport.aws.AwsSdk2Transport;
//import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
//import software.amazon.awssdk.http.SdkHttpClient;
//import software.amazon.awssdk.http.apache.ApacheHttpClient;
//import software.amazon.awssdk.regions.Region;
//
//@Configuration
//public class OpenSearchConfig {
//
//  @Value("${spring.elasticsearch.uris:https://search-domain.region.es.amazonaws.com}")
//  private String openSearchUrl;
//
//  @Value("${spring.elasticsearch.region:ap-northeast-2}")
//  private String awsRegion;
//
//  @Bean
//  public OpenSearchClient openSearchClient() {
//    // IAM ARN 마스터 사용자용 - AWS SDK 2.x 기반
//    SdkHttpClient httpClient = ApacheHttpClient.builder().build();
//
//    AwsSdk2Transport transport = new AwsSdk2Transport(
//        httpClient,
//        extractHostFromUrl(openSearchUrl), // 호스트명만 추출
//        "es", // 서비스명
//        Region.of(awsRegion),
//        AwsSdk2TransportOptions.builder()
//            .setCredentials(DefaultCredentialsProvider.create())
//            .build()
//    );
//
//    return new OpenSearchClient(transport);
//  }
//
//  private String extractHostFromUrl(String url) {
//    // https://search-domain.region.es.amazonaws.com 에서 호스트명만 추출
//    return url.replaceAll("^https?://", "").replaceAll("/$", "");
//  }
//}
