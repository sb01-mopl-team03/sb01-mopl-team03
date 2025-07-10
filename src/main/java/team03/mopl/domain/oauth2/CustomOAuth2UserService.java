package team03.mopl.domain.oauth2;

import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserService;
import team03.mopl.jwt.CustomUserDetails;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserService userService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException{
    OAuth2User oAuth2User = super.loadUser(request);
    String registrationId = request.getClientRegistration().getRegistrationId();

    String email = extractEmail(oAuth2User, registrationId);
    String name = extractName(oAuth2User, registrationId);

    User user = userService.loginOrRegisterOAuth(email, name);

    return new CustomUserDetails(user,oAuth2User.getAttributes());
  }

  private String extractName(OAuth2User user, String provider) {
    Map<String, Object> attributes = user.getAttributes();
    if ("google".equals(provider)) {
      return (String) attributes.get("name");
    }else if ("kakao".equals(provider)) {
      Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
      return (String) properties.get("nickname");
    }
    return "Unknown";
  }

  private String extractEmail(OAuth2User user, String provider) {
    Map<String, Object> attributes = user.getAttributes();
    if ("google".equals(provider)) {
      return (String) attributes.get("email");
    } else if ("kakao".equals(provider)) {
      Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
      Object email = account!=null ? account.get("email") : null;

      if (email != null) {
       return (String) email;
      }else{
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String name = properties != null ? (String) properties.getOrDefault("nickname","kakaoUser") : "kakoUser";
        String fallback = name.replaceAll("\\s+","_") + "@kakao.com";
        return fallback.toLowerCase();
      }
    }
    throw new IllegalArgumentException("Unknown provider: " + provider);
  }

}
