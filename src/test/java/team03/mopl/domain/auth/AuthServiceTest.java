package team03.mopl.domain.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.JwtSessionRepository;
import team03.mopl.jwt.TokenPair;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private JwtSessionRepository sessionRepository;

  @Mock
  private EmailServiceImpl emailService;

  @Mock
  private PasswordEncoder passwordEncoder;

  User user;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .id(UUID.randomUUID())
        .email("test@email.com")
        .name("name")
        .password("password")
        .role(Role.USER)
        .isLocked(false)
        .isTempPassword(false)
        .build();

    ReflectionTestUtils.setField(authService,"refreshTokenExp",3600000L);
    ReflectionTestUtils.setField(authService,"tempPasswordExpirationMinutes",10L);
  }

  @Test
  void login() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password", "password")).thenReturn(true);
    when(jwtProvider.generateToken(user)).thenReturn("access");
    when(jwtProvider.generateRefreshToken(user)).thenReturn("refresh");

    LoginResult result = authService.login("test@email.com", "password");

    assertThat(result.accessToken()).isEqualTo("access");
    assertThat(result.refreshToken()).isEqualTo("refresh");
    assertThat(result.isTempPassword()).isFalse();
    verify(jwtService).save(user,"access","refresh",3600000L);
  }

  @Test
  void loginLockedUser() {
    User lockedUser = user.toBuilder().isLocked(true).build();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(lockedUser));

    IllegalStateException e = assertThrows(IllegalStateException.class, () ->
        authService.login("test@email.com", "pw"));
    assertThat(e.getMessage()).contains("잠긴 계정");
  }


  @Test
  void loginWithExpiredTempPassword() {
    User tempUser = user.toBuilder()
        .isTempPassword(true)
        .tempPasswordExpiredAt(LocalDateTime.now().minusMinutes(1))
        .build();

    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(tempUser));
    when(passwordEncoder.matches(any(), any())).thenReturn(true);

    IllegalStateException e = assertThrows(IllegalStateException.class, () ->
        authService.login("test@email.com", "pw"));
    assertThat(e.getMessage()).contains("Temporary password expired");
  }

  @Test
  void reissueAccessToken() {
    when(jwtService.getAccessTokenByRefreshToken("refresh-token"))
        .thenReturn(Optional.of("new-access"));

    Optional<String> token = authService.reissueAccessToken("refresh-token");

    assertThat(token).contains("new-access");
  }

  @Test
  void refresh() {
    TokenPair pair = new TokenPair("access", "refresh");
    when(jwtService.reissueTokenPair("refresh-token", 3600000L)).thenReturn(pair);

    TokenPair result = authService.refresh("refresh-token");

    assertThat(result).isEqualTo(pair);
  }

  @Test
  void invalidate() {
    authService.invalidateSessionByRefreshToken("refresh", true);
    verify(jwtService).invalidateSessionByRefreshToken("refresh", true);
  }

  @Test
  void resetPassword() {
    when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(any())).thenReturn("encoded-temp");

    authService.resetPassword(user.getEmail());

    verify(userRepository).save(argThat(updated ->
        updated.isTempPassword()
            && updated.getTempPasswordExpiredAt() != null
    ));
    verify(emailService).sendTempPassword(eq(user.getEmail()), anyString());
  }

  @Test
  void changePassword() {
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");

    authService.changePassword(user.getId(), "newpass");

    verify(userRepository).save(argThat(updated ->
        updated.getPassword().equals("encoded-new")
            && !updated.isTempPassword()
            && updated.getTempPasswordExpiredAt() == null
    ));
  }
}