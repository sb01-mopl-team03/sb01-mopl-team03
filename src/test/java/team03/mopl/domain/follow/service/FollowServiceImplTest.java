package team03.mopl.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.util.List;
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
import team03.mopl.domain.follow.entity.Follow;
import team03.mopl.domain.follow.repository.FollowRepository;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.UserResponse;
import team03.mopl.domain.user.UserService;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

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
  @DisplayName("정상 팔로우")
  void follow() {
    // given
    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));
    given(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).willReturn(false);

    // when
    followService.follow(followerId, followingId);

    // then
    then(followRepository).should().save(any(Follow.class));
    then(notificationService).should().sendNotification(
        eq(followingId),
        eq(NotificationType.FOLLOWED),
        contains("팔로우")
    );
  }

  @Test
  @DisplayName("자기 자신 팔로우 불가")
  void followSelfFail() {
    // given
    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));

    // when & then
    assertThrows(CantFollowSelfException.class, () -> followService.follow(followerId, followerId));
  }

  @Test
  @DisplayName("이미 팔로우 중이면 예외")
  void alreadyFollowingFail() {
    // given
    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));
    given(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).willReturn(true);

    // when & then
    assertThrows(AlreadyFollowingException.class, () -> followService.follow(followerId, followingId));
  }

  @Test
  @DisplayName("언팔로우")
  void unfollow() {
    // given
    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));

    // when
    followService.unfollow(followerId, followingId);

    // then
    then(followRepository).should().deleteByFollowerIdAndFollowingId(followerId, followingId);
    then(notificationService).should().sendNotification(
        eq(followingId),
        eq(NotificationType.UNFOLLOWED),
        contains("언팔로우")
    );
  }

  @Test
  @DisplayName("나의 팔로잉 목록 조회")
  void getFollowing() {
    // given
    UUID target1 = UUID.randomUUID();
    UUID target2 = UUID.randomUUID();
    String email1 = "user1@test.com";
    String email2 = "user2@test.com";

    given(followRepository.findAllByFollowerId(followerId)).willReturn(List.of(new Follow(followerId, target1), new Follow(followerId, target2)));
    given(userService.find(target1)).willReturn(new UserResponse(email1, "user1", "USER", false,null));
    given(userService.find(target2)).willReturn(new UserResponse(email2, "user2", "USER", false, null));

    // when
    var result = followService.getFollowing(followerId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).email()).isEqualTo(email1);
    assertThat(result.get(1).email()).isEqualTo(email2);
  }

  @Test
  @DisplayName("나를 팔로우한 사람 목록 조회")
  void getFollowers() {
    // given
    UUID target1 = UUID.randomUUID();
    UUID target2 = UUID.randomUUID();

    String email1 = "user1@test.com";
    String email2 = "user2@test.com";

    given(followRepository.findAllByFollowingId(followingId)).willReturn(List.of(
        new Follow(target1, followingId),
        new Follow(target2, followingId)
    ));

    given(userService.find(target1)).willReturn(new UserResponse(email1, "user1", "USER", false, null));
    given(userService.find(target2)).willReturn(new UserResponse(email2, "user2", "USER", false, null));

    // when
    var result = followService.getFollowers(followingId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).email()).isEqualTo(email1);
    assertThat(result.get(1).email()).isEqualTo(email2);
  }


  @Test
  @DisplayName("isFollowing")
  void isFollowing() {
    // given
    given(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).willReturn(true);

    // when
    boolean result = followService.isFollowing(followerId, followingId);

    // then
    assertThat(result).isTrue();
  }
}
