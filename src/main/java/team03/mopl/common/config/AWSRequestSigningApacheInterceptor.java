package team03.mopl.common.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.regions.Region;

public class AWSRequestSigningApacheInterceptor implements HttpRequestInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(AWSRequestSigningApacheInterceptor.class);

  private final String serviceName;
  private final Aws4Signer signer;
  private final AwsCredentialsProvider credentialsProvider;
  private final Region region;

  public AWSRequestSigningApacheInterceptor(String serviceName,
      Aws4Signer signer,
      AwsCredentialsProvider credentialsProvider,
      String region) {
    this.serviceName = serviceName;
    this.signer = signer;
    this.credentialsProvider = credentialsProvider;
    this.region = Region.of(region);

    logger.info("AWS Interceptor 초기화 - serviceName: {}, region: {}", serviceName, region);
  }

  @Override
  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
    try {
      logger.debug("AWS 서명 프로세스 시작 - Request URI: {}", request.getRequestLine().getUri());

      // 1. 자격 증명 확인
      logger.debug("AWS 자격 증명 확인 중...");
      AwsCredentials credentials;
      try {
        credentials = credentialsProvider.resolveCredentials();
        logger.debug("AWS 자격 증명 획득 성공 - Access Key ID: {}...",
            credentials.accessKeyId().substring(0, Math.min(10, credentials.accessKeyId().length())));
      } catch (Exception e) {
        logger.error("AWS 자격 증명 획득 실패", e);
        throw new HttpException("AWS 자격 증명을 가져올 수 없습니다", e);
      }

      // 2. SDK 요청 변환
      logger.debug("Apache HttpRequest를 SDK 요청으로 변환 중...");
      SdkHttpFullRequest sdkRequest;
      try {
        sdkRequest = ApacheRequestConverter.toSdkRequest(request);
        logger.debug("SDK 요청 변환 성공 - Method: {}, URI: {}",
            sdkRequest.method(), sdkRequest.getUri());
      } catch (Exception e) {
        logger.error("SDK 요청 변환 실패", e);
        throw new HttpException("요청 변환에 실패했습니다", e);
      }

      // 3. 서명 파라미터 생성
      logger.debug("AWS4 서명 파라미터 생성 중...");
      Aws4SignerParams signerParams;
      try {
        signerParams = Aws4SignerParams.builder()
            .signingRegion(region)
            .signingName(serviceName)
            .awsCredentials(credentials)
            .build();
        logger.debug("서명 파라미터 생성 성공 - Service: {}, Region: {}", serviceName, region);
      } catch (Exception e) {
        logger.error("서명 파라미터 생성 실패", e);
        throw new HttpException("서명 파라미터 생성에 실패했습니다", e);
      }

      // 4. 요청 서명
      logger.debug("요청 서명 중...");
      SdkHttpFullRequest signed;
      try {
        signed = signer.sign(sdkRequest, signerParams);
        logger.debug("요청 서명 성공 - Authorization 헤더 존재: {}",
            signed.headers().containsKey("Authorization"));
      } catch (Exception e) {
        logger.error("요청 서명 실패", e);
        throw new HttpException("요청 서명에 실패했습니다", e);
      }

      // 5. 서명된 헤더를 원래 요청에 적용
      logger.debug("서명된 헤더를 원본 요청에 적용 중...");
      int headerCount = 0;
      try {
        for (Map.Entry<String, List<String>> header : signed.headers().entrySet()) {
          if (!header.getValue().isEmpty()) {
            request.setHeader(header.getKey(), header.getValue().get(0));
            headerCount++;

            // Authorization 헤더는 로그에서 일부만 표시
            if ("Authorization".equals(header.getKey())) {
              logger.debug("헤더 적용: {} = {}...", header.getKey(),
                  header.getValue().get(0).substring(0, Math.min(50, header.getValue().get(0).length())));
            } else {
              logger.debug("헤더 적용: {} = {}", header.getKey(), header.getValue().get(0));
            }
          }
        }
        logger.debug("헤더 적용 완료 - 총 {} 개 헤더 적용됨", headerCount);
      } catch (Exception e) {
        logger.error("헤더 적용 실패", e);
        throw new HttpException("헤더 적용에 실패했습니다", e);
      }

      logger.debug("AWS 서명 프로세스 완료");

    } catch (HttpException e) {
      logger.error("AWS 요청 서명 중 HttpException 발생", e);
      throw e;
    } catch (Exception e) {
      logger.error("AWS 요청 서명 중 예상치 못한 오류 발생", e);
      throw new HttpException("AWS 요청 서명 중 오류가 발생했습니다", e);
    }
  }
}
