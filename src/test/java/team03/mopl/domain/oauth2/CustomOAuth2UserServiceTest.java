package team03.mopl.domain.oauth2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserService;
import team03.mopl.jwt.CustomUserDetails;

class CustomOAuth2UserServiceTest {

  @Mock private UserService userService;
  @Mock private DefaultOAuth2UserService delegate;

  private OAuth2AccessToken accessToken;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    accessToken = new OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        "mock-token",
        Instant.now(),
        Instant.now().plusSeconds(3600)
    );
  }

  CustomOAuth2UserService createServiceReturning(OAuth2User mockedOAuth2User) {
    return new CustomOAuth2UserService(userService) {
      @Override
      public OAuth2User loadUser(OAuth2UserRequest request) {
        // 강제 주입된 mock OAuth2User 반환
        return new CustomUserDetails(
            userService.loginOrRegisterOAuth(
                extractEmail(mockedOAuth2User, request.getClientRegistration().getRegistrationId()),
                extractName(mockedOAuth2User, request.getClientRegistration().getRegistrationId())
            ),
            mockedOAuth2User.getAttributes()
        );
      }
    };
  }

  @Test
  void 구글_사용자_정보_정상_처리() {
    String email = "test@example.com";
    String name = "홍길동";
    Map<String, Object> attributes = Map.of("email", email, "name", name);

    OAuth2User mockedOAuth2User = mock(OAuth2User.class);
    when(mockedOAuth2User.getAttributes()).thenReturn(attributes);

    User user = User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .role(Role.USER)
        .build();
    when(userService.loginOrRegisterOAuth(email, name)).thenReturn(user);

    OAuth2UserRequest request = new OAuth2UserRequest(MockProvider.google(), accessToken);
    CustomOAuth2UserService service = createServiceReturning(mockedOAuth2User);

    CustomUserDetails result = (CustomUserDetails) service.loadUser(request);

    assertEquals(email, result.getUsername());
    assertEquals(user.getId(), result.getUser().getId());
  }

  @Test
  void 카카오_사용자_정상_이메일_처리() {
    String email = "kakao@example.com";
    String nickname = "카카오유저";

    Map<String, Object> kakaoAccount = Map.of("email", email);
    Map<String, Object> properties = Map.of("nickname", nickname);
    Map<String, Object> attributes = Map.of(
        "kakao_account", kakaoAccount,
        "properties", properties
    );

    OAuth2User mockedOAuth2User = mock(OAuth2User.class);
    when(mockedOAuth2User.getAttributes()).thenReturn(attributes);

    User user = User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .role(Role.USER)
        .build();
    when(userService.loginOrRegisterOAuth(email, nickname)).thenReturn(user);

    OAuth2UserRequest request = new OAuth2UserRequest(MockProvider.kakao(), accessToken);
    CustomOAuth2UserService service = createServiceReturning(mockedOAuth2User);

    CustomUserDetails result = (CustomUserDetails) service.loadUser(request);
    assertEquals(email, result.getUsername());
  }

  @Test
  void 카카오_이메일없을경우_닉네임기반_이메일생성() {
    String nickname = "김카카오";
    String fallbackEmail = "김카카오@kakao.com".toLowerCase();

    Map<String, Object> kakaoAccount = Map.of();
    Map<String, Object> properties = Map.of("nickname", nickname);
    Map<String, Object> attributes = Map.of(
        "kakao_account", kakaoAccount,
        "properties", properties
    );

    OAuth2User mockedOAuth2User = mock(OAuth2User.class);
    when(mockedOAuth2User.getAttributes()).thenReturn(attributes);

    User user = User.builder()
        .id(UUID.randomUUID())
        .email(fallbackEmail)
        .role(Role.USER)
        .build();
    when(userService.loginOrRegisterOAuth(fallbackEmail, nickname)).thenReturn(user);

    OAuth2UserRequest request = new OAuth2UserRequest(MockProvider.kakao(), accessToken);
    CustomOAuth2UserService service = createServiceReturning(mockedOAuth2User);

    CustomUserDetails result = (CustomUserDetails) service.loadUser(request);
    assertEquals(fallbackEmail, result.getUsername());
  }

  @Test
  void 지원하지않는_소셜로그인_예외발생() {
    Map<String, Object> attributes = Map.of("email", "unknown@example.com");

    OAuth2User mockedOAuth2User = mock(OAuth2User.class);
    when(mockedOAuth2User.getAttributes()).thenReturn(attributes);

    OAuth2UserRequest request = new OAuth2UserRequest(MockProvider.unknown(), accessToken);

    CustomOAuth2UserService service = new CustomOAuth2UserService(userService) {
      @Override
      public OAuth2User loadUser(OAuth2UserRequest request) {
        return super.loadUser(request);
      }
    };

    assertThrows(IllegalArgumentException.class, () -> {
      new CustomOAuth2UserService(userService) {
        @Override
        public OAuth2User loadUser(OAuth2UserRequest request) {
          // 강제로 unknown provider 전달
          OAuth2User user = mock(OAuth2User.class);
          when(user.getAttributes()).thenReturn(attributes);
          extractEmail(user, "naver");
          return null;
        }
      }.loadUser(request);
    });
  }

  private String extractEmail(OAuth2User user, String provider) {
    Map<String, Object> attributes = user.getAttributes();
    if ("google".equals(provider)) {
      return (String) attributes.get("email");
    } else if ("kakao".equals(provider)) {
      Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
      Object email = account != null ? account.get("email") : null;
      if (email != null) {
        return (String) email;
      } else {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String name = properties != null ? (String) properties.getOrDefault("nickname", "kakaoUser") : "kakaoUser";
        return name.replaceAll("\\s+", "_").toLowerCase() + "@kakao.com";
      }
    }
    throw new IllegalArgumentException("Unknown provider: " + provider);
  }

  private String extractName(OAuth2User user, String provider) {
    Map<String, Object> attributes = user.getAttributes();
    if ("google".equals(provider)) {
      return (String) attributes.get("name");
    } else if ("kakao".equals(provider)) {
      Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
      return (String) properties.get("nickname");
    }
    return "Unknown";
  }



  static class MockProvider {
    static ClientRegistration google() {
      return ClientRegistration.withRegistrationId("google")
          .clientId("test")
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri("http://localhost")
          .tokenUri("http://token")
          .authorizationUri("http://auth")
          .userInfoUri("http://userinfo")
          .userNameAttributeName("email")
          .clientName("Google")
          .build();
    }

    static ClientRegistration kakao() {
      return ClientRegistration.withRegistrationId("kakao")
          .clientId("test")
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri("http://localhost")
          .tokenUri("http://token")
          .authorizationUri("http://auth")
          .userInfoUri("http://userinfo")
          .userNameAttributeName("id")
          .clientName("Kakao")
          .build();
    }

    static ClientRegistration unknown() {
      return ClientRegistration.withRegistrationId("naver")
          .clientId("test")
          .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
          .redirectUri("http://localhost")
          .tokenUri("http://token")
          .authorizationUri("http://auth")
          .userInfoUri("http://userinfo")
          .userNameAttributeName("id")
          .clientName("Naver")
          .build();
    }
  }
}
