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
@TestPropertySource(properties = {
    "jwt.secret=testSecretKey12345678901234567890",
    "jwt.access-token-expiration=3600000",
    "jwt.refresh-token-expiration=604800000"
})
class JwtProviderTest {

  @Autowired
  JwtProvider jwtProvider;
  @Autowired
  private JwtBlacklist jwtBlacklist;

  @TestConfiguration
  static class Config {
    @Bean
    public JwtBlacklist jwtBlacklist() {
      return new MemoryJwtBlacklist();
    }

    @Bean
    public JwtProvider jwtProvider() {
      return new JwtProvider(jwtBlacklist());
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

  @Test
  void 리프레쉬토큰_이메일포함() {
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("refresh@example.com")
        .role(Role.USER)
        .build();

    String refreshToken = jwtProvider.generateRefreshToken(user);

    assertTrue(jwtProvider.validateToken(refreshToken));
    assertEquals("refresh@example.com", jwtProvider.extractEmail(refreshToken));
  }

  @Test
  void 유저아이디_추출() {
    ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", 1000L);
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("idtest@example.com")
        .role(Role.USER)
        .build();

    String token = jwtProvider.generateToken(user);

    UUID extractedId = jwtProvider.extractUserId(token);
    assertEquals(user.getId(), extractedId);
  }

  @Test
  void 남은시간_정상토큰이면_양수() {
    ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", 2000L);
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("remain@example.com")
        .role(Role.USER)
        .build();

    String token = jwtProvider.generateToken(user);

    long remaining = jwtProvider.getRemainingTime(token);
    assertTrue(remaining > 0);
  }

  @Test
  void 남은시간_만료토큰_음수() throws InterruptedException {
    ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", 100L);
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("expired@example.com")
        .role(Role.USER)
        .build();

    String token = jwtProvider.generateToken(user);

    Thread.sleep(200);
    long remaining = jwtProvider.getRemainingTime(token);
    assertTrue(remaining < 0);
  }

  @Test
  void 블랙리스트에_등록된_토큰_유효x() {
    ReflectionTestUtils.setField(jwtProvider, "accessTokenExpiration", 1000L);
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("blacklist@example.com")
        .role(Role.USER)
        .build();

    String token = jwtProvider.generateToken(user);
    long remaining = jwtProvider.getRemainingTime(token);
    jwtBlacklist.addBlacklist(token, remaining);

    assertFalse(jwtProvider.validateToken(token));
  }
}