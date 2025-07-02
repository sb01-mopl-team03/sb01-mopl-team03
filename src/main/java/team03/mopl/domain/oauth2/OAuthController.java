package team03.mopl.domain.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.auth.CookieUtil;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserService;
import team03.mopl.jwt.JwtProvider;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthController {

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  private final GoogleOAuthService googleOAuthService;
  private final UserService userService;
  private final JwtProvider jwtProvider;

  @GetMapping("/login/google")
  public void redirectToGoogle(HttpServletResponse response) throws IOException {
    String redirectUrl = googleOAuthService.buildGoogleOAuthUrl();
    response.sendRedirect(redirectUrl);
  }

  @GetMapping("/callback/google")
  public ResponseEntity<?> handleGoogleOAuthCallback(@RequestParam String code,
      HttpServletResponse response) {
    GoogleUserInfo googleUser = googleOAuthService.getUserInfoFromCode(code);

    User user = userService.loginOrRegisterByGoogle(googleUser);

    String accessToken = jwtProvider.generateToken(user);
    String refreshToken = jwtProvider.generateRefreshToken(user);

    Cookie cookie = CookieUtil.createResponseCookie(refreshToken, refreshTokenExpiration);
    response.addCookie(cookie);

    return ResponseEntity.ok(Map.of("accessToken", accessToken));
  }
}
