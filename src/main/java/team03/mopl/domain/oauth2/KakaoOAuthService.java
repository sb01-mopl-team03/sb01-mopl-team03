package team03.mopl.domain.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.TokenPair;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String kakaoClientId;

  @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
  private String kakaoClientSecret;

  @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
  private String kakaoTokenUri;

  @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
  private String kakaoUserInfoUri;

  @Value("${spring.security.oauth2.client.provider.kakao.authorization-uri}")
  private String kakaoAuthorizationUri;

  public String buildKakaoOAuthUrl() {
    return UriComponentsBuilder
        .fromUriString(kakaoAuthorizationUri)
        .queryParam("client_id",kakaoClientId)
        .queryParam("redirect_uri",kakaoRedirectUri)
        .queryParam("response_type","code")
        .build().toUriString();
  }

  public KakaoUserInfo getUserInfoFromKakaoToken(String code) {
    String accessToken = getKakaoAccessToken(code);
    return getKakaoUserInfo(accessToken);
  }

  private KakaoUserInfo getKakaoUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<?> request = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(kakaoUserInfoUri, HttpMethod.GET, request,
        Map.class);

    Map<String , Object> body = response.getBody();

    Map<String, Object> KakaoAccount = (Map<String, Object>) body.get("kakao_account");
    Map<String, Object> properties = (Map<String, Object>) body.get("properties");

    String name = (String) properties.getOrDefault("name","kakaoUser");
    String email = (KakaoAccount != null && KakaoAccount.get("email" )!= null)
      ? (String) KakaoAccount.get("email")
        : name.replaceAll("\\s","_")+"@kakao.com";

    return new KakaoUserInfo(email, name);
  }


  private String getKakaoAccessToken(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("grant_type", "authorization_code");
    body.add("client_id", kakaoClientId);
    body.add("redirect_uri", kakaoRedirectUri);
    body.add("code", code);
    body.add("client_secret", kakaoClientSecret);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
    ResponseEntity<Map> response = restTemplate.postForEntity(
        kakaoTokenUri, request, Map.class
    );

    return (String) response.getBody().get("access_token");
  }
}
