package team03.mopl.domain.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.CustomUserDetails;
import team03.mopl.jwt.TokenPair;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final UserRepository userRepository;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    LoginResult result = authService.login(request.email(), request.password());

    Cookie cookie = CookeUtil.createResponseCookie(result.refreshToken(), refreshTokenExpiration);
    response.addCookie(cookie);


    return ResponseEntity.ok().body(new LoginResponse(result.accessToken(),result.isTempPassword()));
    //isTempPassword true 면 change-password 로
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@CookieValue(value = "refresh", required = false) String refreshToken) {
    if (refreshToken == null) {
      return ResponseEntity.badRequest().body("No refresh token");
    }

    authService.invalidateSessionByRefreshToken(refreshToken);

    return ResponseEntity.ok("LOGOUT");
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refresh(
      @CookieValue(value = "refresh", required = false) String refreshToken,
      HttpServletResponse response) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token found");
    }
    try {
      TokenPair tokenPair = authService.refresh(refreshToken);

      Cookie cookie = CookeUtil.createResponseCookie(tokenPair.getRefreshToken(),
          refreshTokenExpiration);
      response.addCookie(cookie);

      return ResponseEntity.ok().body(tokenPair.getAccessToken());
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("세션 없음 또는 만료됨");
    }
  }

  @PostMapping("/me")
  public ResponseEntity<?> reissueAccessToken(
      @CookieValue(value = "refresh", required = false) String refreshToken) {
    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token");
    }

    return
        authService.reissueAccessToken(refreshToken)
            .map(ResponseEntity::ok)
            .orElseGet(
                () -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token invalid"));
  }

  @PostMapping("/temp-password")
  public ResponseEntity<?> resetPassword(@RequestBody TempPasswordRequest request){
    authService.resetPassword(request.email());
    return ResponseEntity.ok("임시 비밀번호 발급");
  }

  @PostMapping("/change-password")
  public ResponseEntity<?> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody ChangePasswordRequest request){
    authService.changePassword(userDetails.getId(),request.newPassword());
    return ResponseEntity.ok("비밀번호 변경완료");
  }
}
