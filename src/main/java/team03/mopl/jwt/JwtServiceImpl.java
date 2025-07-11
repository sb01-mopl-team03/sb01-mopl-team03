package team03.mopl.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.domain.user.User;

@RequiredArgsConstructor
@Service
public class JwtServiceImpl implements JwtService {

  private final JwtSessionRepository jwtSessionRepository;
  private final JwtProvider jwtProvider;
  private final JwtBlacklist jwtBlacklist;

  @Override
  @Transactional
  public void save(User user, String accessToken, String refreshToken,
      long refreshTokenExpirationMillis) {
    jwtSessionRepository.findByUser(user).ifPresent(jwtSessionRepository::delete);

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusNanos(refreshTokenExpirationMillis * 1_000_000);

    JwtSession session = JwtSession.builder()
        .user(user)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .createdAt(now)
        .expiresAt(expiresAt)
        .isActive(true)
        .build();

    jwtSessionRepository.save(session);
  }

  @Override
  @Transactional
  public void delete(User user) {
    jwtSessionRepository.deleteByUser(user);
  }

  @Override
  public Optional<String> getAccessTokenByRefreshToken(String refreshToken) {
    return jwtSessionRepository.findByRefreshToken(refreshToken)
        .filter(JwtSession::isActive)
        .filter(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
        .map(session -> jwtProvider.generateToken(session.getUser()));
  }

  @Override
  @Transactional
  public TokenPair reissueTokenPair(String refreshToken, long refreshTokenExpiration) {
    return jwtSessionRepository.findByRefreshToken(refreshToken)
        .filter(JwtSession::isActive)
        .filter(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
        .map(session -> {
          User user = session.getUser();
          jwtSessionRepository.delete(session);

          String newAccessToken = jwtProvider.generateToken(user);
          String newRefreshToken = jwtProvider.generateRefreshToken(user);

          this.save(user, newAccessToken, newRefreshToken, refreshTokenExpiration);
          return new TokenPair(newAccessToken, newRefreshToken);
        }).orElseThrow(() -> new RuntimeException("Refresh token 만료"));
  }

  @Override
  public void invalidateSessionByRefreshToken(String refreshToken, boolean useBlacklist) {
    jwtSessionRepository.findByRefreshToken(refreshToken).ifPresent(session -> {
      if (useBlacklist) {
        String accessToken=session.getAccessToken();
        long exp = jwtProvider.getClaims(accessToken).getExpiration().getTime();
        jwtBlacklist.addBlacklist(session.getAccessToken(), exp);
      }
      jwtSessionRepository.delete(session);
    });
  }
}
