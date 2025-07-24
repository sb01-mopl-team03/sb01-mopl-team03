package team03.mopl.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

class CustomUserDetailsServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CustomUserDetailsService customUserDetailsService;

  private final UUID userId = UUID.randomUUID();
  private final String email = "test@example.com";

  private User testUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    testUser = User.builder()
        .id(userId)
        .email(email)
        .password("password")
        .build();
  }

  @Test
  void loadUserById() {
    when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

    CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);

    assertEquals(email, userDetails.getUsername());
    verify(userRepository).findById(userId);
  }

  @Test
  void loadUserById_실패() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserById(userId));
  }

  @Test
  void loadUserByUsername() {
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

    assertEquals(email, userDetails.getUsername());
    verify(userRepository).findByEmail(email);
  }

  @Test
  void loadUserByUsername_실패() {
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername(email));
  }
}