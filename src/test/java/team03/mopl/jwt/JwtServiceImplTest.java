package team03.mopl.jwt;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;

class JwtServiceImplTest {

 @Mock
     private JwtSessionRepository jwtSessionRepository;

 @Mock
     private JwtProvider jwtProvider;

 @InjectMocks
     private JwtServiceImpl jwtService;

  User user;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @DisplayName("JWT 저장" )
  @Test
  void save_shouldPersistJwtSession() {
    // given
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("save@example.com")
        .role(Role.USER)
        .build();

    String accessToken = "access-token";
    String refreshToken = "refresh-token";
    long expirationMillis = 1000L * 60 * 60; // 1시간

    // when
    jwtService.save(user, accessToken, refreshToken, expirationMillis);

    // then
    verify(jwtSessionRepository).save(any(JwtSession.class));
  }

  @DisplayName("사용자 정보를 기반으로 JWT  삭제")
  @Test
  void delete_shouldRemoveUserSessions() {
    // given
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("delete@example.com")
        .role(Role.USER)
        .build();
    JwtSession mockSession = mock(JwtSession.class);
    when(mockSession.getAccessToken()).thenReturn("access-token");
    when(jwtSessionRepository.findByUser(user)).thenReturn(Optional.of(mockSession));

    // when
    jwtService.delete(user);

    // then
    verify(jwtSessionRepository).findByUser(user);
    verify(jwtSessionRepository).delete(mockSession);
  }

  @DisplayName("유요한 세션이 존재할 경우, 새로운 accesstoken을 발급")
  @Test
  void getAccessTokenByRefreshToken_shouldReturnAccessToken_whenSessionValid() {
    // given
    String refreshToken = "valid-refresh-token";
    String expectedAccessToken = "new-access-token";
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .role(Role.USER)
        .build();

    JwtSession session = JwtSession.builder()
        .user(user)
        .refreshToken(refreshToken)
        .accessToken("old-access-token")
        .createdAt(LocalDateTime.now().minusMinutes(10))
        .expiresAt(LocalDateTime.now().plusMinutes(30))
        .isActive(true)
        .build();

    when(jwtSessionRepository.findByRefreshToken(refreshToken))
        .thenReturn(Optional.of(session));
    when(jwtProvider.generateToken(user)).thenReturn(expectedAccessToken);

    // when
    Optional<String> result = jwtService.getAccessTokenByRefreshToken(refreshToken);

    // then
    assertThat(result).contains(expectedAccessToken);
    verify(jwtProvider).generateToken(user);
  }

  @DisplayName("세션이 존재하지만 만료된 경우, accesstoken을 발급 x")
  @Test
  void getAccessTokenByRefreshToken_shouldReturnEmpty_whenSessionExpired() {
    // given
    String refreshToken = "expired-refresh-token";
    JwtSession session = JwtSession.builder()
        .refreshToken(refreshToken)
        .expiresAt(LocalDateTime.now().minusMinutes(1))
        .isActive(true)
        .build();

    when(jwtSessionRepository.findByRefreshToken(refreshToken))
        .thenReturn(Optional.of(session));

    // when
    Optional<String> result = jwtService.getAccessTokenByRefreshToken(refreshToken);

    // then
    assertThat(result).isEmpty();
    verify(jwtProvider, never()).generateToken(any());
  }

  @DisplayName("세션 x, accesstoken 발급 x")
  @Test
  void getAccessTokenByRefreshToken_shouldReturnEmpty_whenSessionNotFound() {
    // given
    String refreshToken = "nonexistent-token";
    when(jwtSessionRepository.findByRefreshToken(refreshToken))
        .thenReturn(Optional.empty());

    // when
    Optional<String> result = jwtService.getAccessTokenByRefreshToken(refreshToken);

    // then
    assertThat(result).isEmpty();
    verify(jwtProvider, never()).generateToken(any());
  }

  @DisplayName("리프레시 토큰으로 세션을 무효화")
  @Test
  void invalidateSessionByRefreshToken_shouldDeleteByRefreshToken() {
    // given
    String refreshToken = "some-refresh-token";

    User user = User.builder()
        .id(UUID.randomUUID())
        .email("test@example.com")
        .role(Role.USER)
        .build();

    JwtSession session = JwtSession.builder()
        .user(user)
        .refreshToken(refreshToken)
        .accessToken("access-token")
        .expiresAt(LocalDateTime.now().plusMinutes(10))
        .isActive(true)
        .build();

    when(jwtSessionRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(session));

    // when
    jwtService.invalidateSessionByRefreshToken(refreshToken, false);

    // then
    verify(jwtSessionRepository).delete(any(JwtSession.class));
  }

  @DisplayName("유효한 refresh token이 있으면 새로운 토큰 쌍을 재발급")
  @Test
  void reissueTokenPair_shouldReissueTokens_whenSessionValid() {
    // given
    String oldRefreshToken = "old-refresh-token";
    String newAccessToken = "new-access-token";
    String newRefreshToken = "new-refresh-token";
    long expirationMillis = 3600000L;

    User user = User.builder()
        .id(UUID.randomUUID())
        .email("reissue@example.com")
        .role(Role.USER)
        .build();

    JwtSession session = JwtSession.builder()
        .user(user)
        .refreshToken(oldRefreshToken)
        .expiresAt(LocalDateTime.now().plusMinutes(10))
        .isActive(true)
        .build();

    when(jwtSessionRepository.findByRefreshToken(oldRefreshToken)).thenReturn(Optional.of(session));
    when(jwtProvider.generateToken(user)).thenReturn(newAccessToken);
    when(jwtProvider.generateRefreshToken(user)).thenReturn(newRefreshToken);

    // when
    TokenPair tokenPair = jwtService.reissueTokenPair(oldRefreshToken, expirationMillis);

    // then
    assertThat(tokenPair.getAccessToken()).isEqualTo(newAccessToken);
    assertThat(tokenPair.getRefreshToken()).isEqualTo(newRefreshToken);
    verify(jwtSessionRepository).delete(session);
    verify(jwtSessionRepository).save(any(JwtSession.class));
  }

  @DisplayName("만료된 세션으로 토큰을 재발급하면 예외가 발생")
  @Test
  void reissueTokenPair_shouldThrowException_whenSessionExpired() {
    // given
    String expiredRefreshToken = "expired-token";
    JwtSession expiredSession = JwtSession.builder()
        .refreshToken(expiredRefreshToken)
        .expiresAt(LocalDateTime.now().minusMinutes(5))
        .isActive(true)
        .build();

    when(jwtSessionRepository.findByRefreshToken(expiredRefreshToken))
        .thenReturn(Optional.of(expiredSession));

    // when & then
    org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
        () -> jwtService.reissueTokenPair(expiredRefreshToken, 3600000L));
  }
}