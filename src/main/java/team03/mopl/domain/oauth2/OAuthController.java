package team03.mopl.domain.oauth2;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuthController {

  private final KakaoOAuthService kakaoOAuthService;
  private final GoogleOAuthService googleOAuthService;
  private final OAuthLoginService oAuthLoginService;

  @GetMapping("/login/google")
  public void redirectToGoogle(HttpServletResponse response) throws IOException {
    String redirectUrl = googleOAuthService.buildGoogleOAuthUrl();
    response.sendRedirect(redirectUrl);
  }

  @GetMapping("/login/kakao")
  public void redirectToKakao(HttpServletResponse response) throws IOException {
    String redirectUrl = kakaoOAuthService.buildKakaoOAuthUrl();
    response.sendRedirect(redirectUrl);
  }

  @GetMapping("/callback/google")
  public ResponseEntity<?> handleGoogleOAuthCallback(@RequestParam String code,
      HttpServletResponse response) {
    return oAuthLoginService.loginByGoogle(code, response);
  }

  @GetMapping("/callback/kakao")
  public ResponseEntity<?> handleKakaoOAuthCallback(@RequestParam String code,
      HttpServletResponse response) {
   return oAuthLoginService.loginByKakao(code, response);
  }
}
