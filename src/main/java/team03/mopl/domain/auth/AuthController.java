package team03.mopl.domain.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final JwtProvider jwtProvider;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    User user=userRepository.findByEmail(request.email())
        .orElseThrow(UserNotFoundException::new);

    jwtService.delete(user);

    String accessToken = jwtProvider.generateToken(user);
    String refreshToken = jwtProvider.generateRefreshToken(user);

    jwtService.save(user,accessToken,refreshToken,refreshTokenExpiration);

    return ResponseEntity.ok().body(accessToken);
  }
}
