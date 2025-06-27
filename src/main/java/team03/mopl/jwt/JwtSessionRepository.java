package team03.mopl.jwt;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.user.User;

public interface JwtSessionRepository extends JpaRepository<JwtSession, UUID> {

  void deleteByUser(User user);

  boolean existsByAccessToken(String token);

  Optional<JwtSession> findByRefreshToken(String refreshToken);

  void deleteByRefreshToken(String refreshToken);
}
