package team03.mopl.jwt;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.domain.user.User;

@RequiredArgsConstructor
@Service
public class JwtServiceImpl implements JwtService {

  private final JwtSessionRepository jwtSessionRepository;

  public void save(User user,String accessToken,String refreshToken,long refreshTokenExpirationMillis) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusNanos(refreshTokenExpirationMillis*1_000_000);

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

  public void delete(User user){
    jwtSessionRepository.deleteByUser(user);
  }
}
