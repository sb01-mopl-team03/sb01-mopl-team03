package team03.mopl.domain.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
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
import team03.mopl.domain.follow.entity.Follow;
import team03.mopl.domain.follow.repository.FollowRepository;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
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
    then(notificationService).should().sendNotification(
        argThat(dto ->
            dto.getReceiverId().equals(followingId) &&
                dto.getNotificationType() == NotificationType.FOLLOWED &&
                dto.getContent().contains("팔로우")
        )
    );

  }

  @Test
  @DisplayName("언팔로우 - 정상 케이스")
  void unfollow() {
    // given
    given(userRepository.findById(followerId)).willReturn(Optional.of(follower));
    given(userRepository.findById(followingId)).willReturn(Optional.of(following));
    given(followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)).willReturn(true);

    // when
    followService.unfollow(followerId, followingId);

    // then
    then(followRepository).should().deleteByFollowerIdAndFollowingId(followerId, followingId);
    then(notificationService).should().sendNotification(
        argThat(dto ->
            dto.getReceiverId().equals(followingId) &&
                dto.getNotificationType() == NotificationType.UNFOLLOWED &&
                dto.getContent().contains("언팔로우")
        )
    );

  }


  @Test
  @DisplayName("나의 팔로잉 목록 조회")
  void getFollowing() {
    // given
    User user1 = User.builder()
        .id(UUID.randomUUID())
        .email("user1@test.com")
        .name("user1")
        .password("pw")
        .role(Role.USER)
        .build();
    User user2 = User.builder()
        .id(UUID.randomUUID())
        .email("user2@test.com")
        .name("user2")
        .password("pw")
        .role(Role.USER)
        .build();

    given(followRepository.findAllByFollowerId(followerId))
        .willReturn(List.of(
            new Follow(followerId, user1.getId()),
            new Follow(followerId, user2.getId())
        ));
    given(userRepository.findById(user1.getId())).willReturn(Optional.of(user1));
    given(userRepository.findById(user2.getId())).willReturn(Optional.of(user2));

    // when
    var result = followService.getFollowing(followerId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).email()).isEqualTo(user1.getEmail());
    assertThat(result.get(1).email()).isEqualTo(user2.getEmail());
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

    given(userRepository.findById(target1)).willReturn(Optional.of(
        User.builder()
            .id(target1)
            .email(email1)
            .name("user1")
            .password("pw")
            .role(Role.USER)
            .build()
    ));

    given(userRepository.findById(target2)).willReturn(Optional.of(
        User.builder()
            .id(target2)
            .email(email2)
            .name("user2")
            .password("pw")
            .role(Role.USER)
            .build()
    ));

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
