package team03.mopl.domain.auth;

import java.util.Optional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.TokenPair;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final JwtProvider jwtProvider;

  @Value("${jwt.refresh-token-expriation}")
  private long refreshTokenExp;

  public TokenPair login(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);

    jwtService.delete(user);

    String accessToken = jwtProvider.generateToken(user);
    String refreshToken = jwtProvider.generateRefreshToken(user);

    jwtService.save(user, accessToken, refreshToken, refreshTokenExp);

    return new TokenPair(accessToken, refreshToken);
  }

  public TokenPair refresh(String refreshToken) {
    return jwtService.reissueTokenPair(refreshToken, refreshTokenExp);
  }

  public Optional<String> reissueAccessToken(String refreshToken) {
    return jwtService.getAccessTokenByRefreshToken(refreshToken);
  }

  public void invalidateSessionByRefreshToken(String refreshToken) {
    jwtService.invalidateSessionByRefreshToken(refreshToken);
  }
}
