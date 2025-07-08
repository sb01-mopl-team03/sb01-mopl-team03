package team03.mopl.domain.admin;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.UserResponse;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.JwtSession;
import team03.mopl.jwt.JwtSessionRepository;


@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @InjectMocks
  private AdminService adminService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtService jwtService;

  @Mock
  private JwtSessionRepository jwtSessionRepository;

  private UUID userId;
  private User user;


  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .email("admin@mopl.com")
        .name("admin")
        .role(Role.ADMIN)
        .isLocked(false)
        .password("password")
        .build();
  }

  @Test
  void changeRole() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtSessionRepository.findFirstByUserId(userId)).thenReturn(Optional.of(createSession(user)));

    UserResponse response = adminService.changeRole(userId, Role.ADMIN);

    // then
    assertThat(response.role()).isEqualTo("ADMIN");
    verify(jwtService).invalidateSessionByRefreshToken("refresh", true);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void lockUser() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(jwtSessionRepository.findFirstByUserId(userId)).thenReturn(Optional.of(createSession(user)));

    // when
    UserResponse response = adminService.lockUser(userId);

    // then
    assertThat(response.isLocked()).isTrue();
    verify(jwtService).invalidateSessionByRefreshToken("refresh", true);
    verify(userRepository).save(any(User.class));
  }

  @Test
  void unlockUser() {
    // given
    User lockedUser = user.toBuilder().isLocked(true).build();
    when(userRepository.findById(userId)).thenReturn(Optional.of(lockedUser));

    // when
    UserResponse response = adminService.unlockUser(userId);

    // then
    assertThat(response.isLocked()).isFalse();
    verify(userRepository).save(any(User.class));
  }

  @Test
  void userNotFound() {
    // given
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // then
    assertThrows(UserNotFoundException.class, () -> adminService.lockUser(userId));
    assertThrows(UserNotFoundException.class, () -> adminService.unlockUser(userId));
    assertThrows(UserNotFoundException.class, () -> adminService.changeRole(userId, Role.ADMIN));
  }

  private JwtSession createSession(User user) {
    return JwtSession.builder()
        .id(UUID.randomUUID())
        .user(user)
        .accessToken("access")
        .refreshToken("refresh")
        .createdAt(LocalDateTime.now())
        .expiresAt(LocalDateTime.now().plusDays(1))
        .isActive(true)
        .build();
  }
}