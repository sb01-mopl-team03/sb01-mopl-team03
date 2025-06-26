package team03.mopl.domain.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.TokenPair;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JwtService jwtService;
  private AuthService authService;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
    TokenPair tokenPair = authService.login(request.email());

    Cookie cookie = CookeUtil.createResponseCookie(tokenPair.getRefreshToken(),
        refreshTokenExpiration);
    response.addCookie(cookie);

    return ResponseEntity.ok().body(tokenPair.getAccessToken());
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
}
