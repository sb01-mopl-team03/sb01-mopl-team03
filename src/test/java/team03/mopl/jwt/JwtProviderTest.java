package team03.mopl.jwt;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;

@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
    "jwt.secret=testSecretKey12345678901234567890",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=604800000"
})
class JwtProviderTest {

  @Autowired
  JwtProvider jwtProvider;

  @TestConfiguration
  static class Config {
    @Bean
    public JwtProvider jwtProvider() {
      return new JwtProvider();
    }
  }

  @Test
  void generateAndValidateToken() {
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .role(Role.USER)
        .build();

    String accessToken = jwtProvider.generateToken(user);

    assertTrue(jwtProvider.validateToken(accessToken));
    assertEquals("test@example.com", jwtProvider.extractEmail(accessToken));
  }

  @Test
  void validateToken_shouldReturnFalse_forExpired() throws InterruptedException {
    ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", 1L);
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("expired@example.com")
        .role(Role.USER)
        .build();
    String token = jwtProvider.generateToken(user);

    Thread.sleep(10);  // 잠시 대기 후 만료됨
    assertFalse(jwtProvider.validateToken(token));
  }
}