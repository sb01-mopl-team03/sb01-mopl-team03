package team03.mopl.domain.oauth2;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

  private final RestTemplate restTemplate;

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String clientSecret;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String redirectUri;

  private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
  private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";

  public String buildGoogleOAuthUrl(){
    return UriComponentsBuilder
        .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
        .queryParam("client_id", clientId)
        .queryParam("redirect_uri", redirectUri)
        .queryParam("response_type", "code")
        .queryParam("scope", "email profile")
        .build().toUriString();
  }

  public GoogleUserInfo getUserInfoFromCode(String code){
    String accessToken= fetchAccessToken(code);
    return fetchUserInfo(accessToken);
  }

  private GoogleUserInfo fetchUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<?> request = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        GOOGLE_USER_INFO_URL, HttpMethod.GET, request, Map.class
    );

    Map<String, Object> body = response.getBody();
    return new GoogleUserInfo(
        (String) body.get("email"),
        (String) body.get("name"),
        (String) body.get("picture")
    );
  }

  private String fetchAccessToken(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("code", code);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("redirect_uri", redirectUri);
    body.add("grant_type", "authorization_code");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(GOOGLE_TOKEN_URL,request,Map.class);
    return (String) response.getBody().get("access_token");
  }
}
