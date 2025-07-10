package team03.mopl.domain.follow.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team03.mopl.common.exception.follow.AlreadyFollowingException;
import team03.mopl.common.exception.follow.CantFollowSelfException;
import team03.mopl.common.exception.follow.FollowNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.follow.repository.FollowRepository;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.UserService;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplFailTest {

  @Mock
  private FollowRepository followRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private UserService userService;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private FollowServiceImpl followService;

  private UUID followerId;
  private UUID followingId;
  private User follower;
  private User following;

  @BeforeEach
  void setUp() {
    followerId = UUID.randomUUID();
    followingId = UUID.randomUUID();

    follower = User.builder()
        .id(followerId)
        .email("follower@test.com")
        .name("follower")
        .password("pw")
        .build();

    following = User.builder()
        .id(followingId)
        .email("following@test.com")
        .name("following")
        .password("pw")
        .build();
  }

  @Test
  @DisplayName("팔로우 - 존재하지 않는 follower 예외")
  void follow_shouldThrowUserNotFoundException_whenFollowerNotFound() {
    when(userRepository.findById(followerId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      followService.follow(followerId, followingId);
    });
  }

  @Test
  @DisplayName("팔로우 - 존재하지 않는 following 예외")
  void follow_shouldThrowUserNotFoundException_whenFollowingNotFound() {
    when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
    when(userRepository.findById(followingId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      followService.follow(followerId, followingId);
    });
  }

  @Test
  @DisplayName("팔로우 - 자기 자신을 팔로우할 수 없음 예외")
  void follow_shouldThrowCantFollowSelfException_whenFollowerEqualsFollowing() {
    UUID sameId = followerId;

    when(userRepository.findById(sameId)).thenReturn(Optional.of(follower));

    assertThrows(CantFollowSelfException.class, () -> {
      followService.follow(sameId, sameId);
    });
  }

  @Test
  @DisplayName("팔로우 - 이미 팔로우한 경우 예외")
  void follow_shouldThrowAlreadyFollowingException_whenAlreadyFollowing() {
    when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
    when(userRepository.findById(followingId)).thenReturn(Optional.of(following));
    when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(true);

    assertThrows(AlreadyFollowingException.class, () -> {
      followService.follow(followerId, followingId);
    });
  }


  @Test
  @DisplayName("언팔로우 - 존재하지 않는 follower 예외")
  void unfollow_shouldThrowUserNotFoundException_whenFollowerNotFound() {
    when(userRepository.findById(followerId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      followService.unfollow(followerId, followingId);
    });
  }

  @Test
  @DisplayName("언팔로우 - 존재하지 않는 following 예외")
  void unfollow_shouldThrowUserNotFoundException_whenFollowingNotFound() {
    when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
    when(userRepository.findById(followingId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      followService.unfollow(followerId, followingId);
    });
  }

  @Test
  @DisplayName("언팔로우 - 팔로우 관계가 없을 때 예외")
  void unfollow_shouldThrowFollowNotFoundException_whenFollowRelationNotFound() {
    when(userRepository.findById(followerId)).thenReturn(Optional.of(follower));
    when(userRepository.findById(followingId)).thenReturn(Optional.of(following));
    when(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).thenReturn(false);

    assertThrows(FollowNotFoundException.class, () -> {
      followService.unfollow(followerId, followingId);
    });
  }

}