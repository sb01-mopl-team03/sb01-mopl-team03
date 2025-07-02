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
  private String googleClientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String googleClientSecret;

  @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
  private String googleRedirectUri;

  @Value("${spring.security.oauth2.client.provider.google.token-uri}")
  private String googleTokenUri;

  @Value("${spring.security.oauth2.client.provider.google.user-info-uri}")
  private String googleUserInfoUri;

  @Value("${spring.security.oauth2.client.provider.google.authorization-uri}")
  private String googleAuthorizationUri;

  public String buildGoogleOAuthUrl(){
    return UriComponentsBuilder
        .fromUriString(googleAuthorizationUri)
        .queryParam("client_id", googleClientId)
        .queryParam("redirect_uri", googleRedirectUri)
        .queryParam("response_type", "code")
        .queryParam("scope", "email profile")
        .build().toUriString();
  }

  public GoogleUserInfo getUserInfoFromGoogleCode(String code){
    String accessToken= getGoogleAccessToken(code);
    return getGoogleUserInfo(accessToken);
  }

  private GoogleUserInfo getGoogleUserInfo(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<?> request = new HttpEntity<>(headers);

    ResponseEntity<Map> response = restTemplate.exchange(
        googleUserInfoUri, HttpMethod.GET, request, Map.class
    );

    Map<String, Object> body = response.getBody();
    return new GoogleUserInfo(
        (String) body.get("email"),
        (String) body.get("name"),
        (String) body.get("picture")
    );
  }

  private String getGoogleAccessToken(String code) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("code", code);
    body.add("client_id", googleClientId);
    body.add("client_secret", googleClientSecret);
    body.add("redirect_uri", googleRedirectUri);
    body.add("grant_type", "authorization_code");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(
        googleTokenUri,request,Map.class);
    return (String) response.getBody().get("access_token");
  }
}
