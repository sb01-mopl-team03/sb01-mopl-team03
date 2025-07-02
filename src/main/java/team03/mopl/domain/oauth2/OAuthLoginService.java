package team03.mopl.domain.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import team03.mopl.domain.auth.CookieUtil;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserService;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;

@Service
@RequiredArgsConstructor
public class OAuthLoginService {

  private final GoogleOAuthService googleOAuthService;
  private final KakaoOAuthService kakaoOAuthService;
  private final UserService userService;
  private final JwtProvider jwtProvider;
  private final JwtService jwtService;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  public ResponseEntity<?> loginByGoogle(String code, HttpServletResponse response){
    GoogleUserInfo googleUser= googleOAuthService.getUserInfoFromGoogleCode(code);
    return authenticateOAuthUser(googleUser.email(), googleUser.name(), response);
  }

  public ResponseEntity<?> loginByKakao(String code, HttpServletResponse response){
    KakaoUserInfo kakaoUserUser= kakaoOAuthService.getUserInfoFromKakaoToken(code);
    return authenticateOAuthUser(kakaoUserUser.email(), kakaoUserUser.name(), response);
  }

  private ResponseEntity<?> authenticateOAuthUser(String email, String name, HttpServletResponse response) {
    User user = userService.loginOrRegisterOAuth(email, name);
    String accessToken = jwtProvider.generateToken(user);
    String refreshToken = jwtProvider.generateRefreshToken(user);
    jwtService.save(user,accessToken,refreshToken,refreshTokenExpiration);

    Cookie cookie= CookieUtil.createResponseCookie(refreshToken,refreshTokenExpiration);
    response.addCookie(cookie);

    return ResponseEntity.ok(Map.of("access_token", accessToken));
  }
}
