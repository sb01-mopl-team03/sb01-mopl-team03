package team03.mopl.common.config;

import java.io.IOException;
import software.amazon.awssdk.http.*;
import org.apache.http.HttpRequest;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class ApacheRequestConverter {

  public static SdkHttpFullRequest toSdkRequest(HttpRequest request) throws IOException {
    String uriString = request.getRequestLine().getUri();
    String method = request.getRequestLine().getMethod();

    // 절대 URI가 아닌 경우 처리
    URI uri;
    if (!uriString.startsWith("http")) {
      // Host 헤더에서 호스트 정보 가져오기
      String hostHeader = null;
      for (org.apache.http.Header header : request.getAllHeaders()) {
        if ("Host".equalsIgnoreCase(header.getName())) {
          hostHeader = header.getValue();
          break;
        }
      }

      if (hostHeader != null) {
        // HTTPS를 기본으로 사용 (AWS OpenSearch는 HTTPS)
        String scheme = "https";
        uriString = scheme + "://" + hostHeader + uriString;
      } else {
        throw new IllegalArgumentException("Cannot determine host for URI: " + uriString);
      }
    }

    uri = URI.create(uriString);

    SdkHttpFullRequest.Builder builder = SdkHttpFullRequest.builder()
        .method(SdkHttpMethod.fromValue(method))
        .uri(uri)
        .encodedPath(uri.getRawPath() != null ? uri.getRawPath() : "/");

    // Query String 처리
    if (uri.getRawQuery() != null && !uri.getRawQuery().isEmpty()) {
      Map<String, List<String>> params = Arrays.stream(uri.getRawQuery().split("&"))
          .filter(param -> !param.isEmpty())
          .map(kv -> kv.split("=", 2))
          .collect(Collectors.groupingBy(
              kv -> kv[0],
              Collectors.mapping(kv -> kv.length > 1 ? kv[1] : "", Collectors.toList())
          ));
      builder.rawQueryParameters(params);
    }

    // Headers 처리
    for (org.apache.http.Header header : request.getAllHeaders()) {
      builder.putHeader(header.getName(), header.getValue());
    }

    // Body 처리
    if (request instanceof HttpEntityEnclosingRequest entityRequest &&
        entityRequest.getEntity() != null) {

      byte[] content = EntityUtils.toByteArray(entityRequest.getEntity());
      builder.contentStreamProvider(() -> new java.io.ByteArrayInputStream(content));
    }

    return builder.build();
  }
}
