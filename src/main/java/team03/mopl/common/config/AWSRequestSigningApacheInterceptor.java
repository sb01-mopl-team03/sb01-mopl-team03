package team03.mopl.common.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.http.SdkHttpFullRequest;
import software.amazon.awssdk.regions.Region;

public class AWSRequestSigningApacheInterceptor implements HttpRequestInterceptor {
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
  }

  @Override
  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
    // Apache HttpRequest를 SdkHttpFullRequest로 변환 (직접 구현 필요)
    SdkHttpFullRequest sdkRequest = ApacheRequestConverter.toSdkRequest(request);

    Aws4SignerParams signerParams = Aws4SignerParams.builder()
        .signingRegion(region)
        .signingName(serviceName)
        .awsCredentials(credentialsProvider.resolveCredentials())
        .build();

    SdkHttpFullRequest signed = signer.sign(sdkRequest, signerParams);

    // 서명된 헤더를 원래 Apache 요청에 덮어씀
    for (Map.Entry<String, List<String>> header : signed.headers().entrySet()) {
      request.setHeader(header.getKey(), header.getValue().get(0));
    }
  }
}
