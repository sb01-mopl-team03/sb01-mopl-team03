package team03.mopl.domain.follow.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.follow.AlreadyFollowingException;
import team03.mopl.common.exception.follow.CantFollowSelfException;
import team03.mopl.common.exception.follow.FollowNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.follow.dto.FollowResponse;
import team03.mopl.domain.follow.entity.Follow;
import team03.mopl.domain.follow.repository.FollowRepository;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserService;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final UserService userService;
  private final NotificationService notificationService;

  @Override
  public void follow(UUID followerId, UUID followingId) {
    //팔로우 하는 사람
    User follower = userRepository.findById(followerId).orElseThrow(UserNotFoundException::new);
    //팔로우 당하는 사람
    User following = userRepository.findById(followingId).orElseThrow(UserNotFoundException::new);
    if( followingId.equals(followerId) ) {
      throw new CantFollowSelfException(); //자신을 팔로잉할 수 없음
    }
    boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    if( alreadyFollowing ) {
      throw new AlreadyFollowingException(); //이미 팔로잉 중
    }
    Follow follow = new Follow(followerId, followingId);
    followRepository.save(follow);

    // 알림 전송 추가
    notificationService.sendNotification(following.getId(), NotificationType.FOLLOWED, following.getName()+"이(가) 팔로우 했습니다.");

  }

  @Override
  public void unfollow(UUID followerId, UUID followingId) {
    User unFollower = userRepository.findById(followerId).orElseThrow(FollowNotFoundException::new);
    User unFollowing = userRepository.findById(followingId).orElseThrow(FollowNotFoundException::new);
    followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
    // 알림 전송 추가
    notificationService.sendNotification(unFollowing.getId(), NotificationType.UNFOLLOWED, unFollowing.getName()+"이(가) 언팔로우 했습니다.");

  }

  //나의 팔로잉 목록
  @Override
  public List<FollowResponse> getFollowing(UUID userId) {
    List<UUID> list = followRepository.findAllByFollowerId(userId).stream().map(Follow::getFollowingId).toList();
    return list.stream().map( id -> FollowResponse.fromUserResponse(id, userService.find(id))).toList();
  }

  //나를 팔로우하는 사람들 목록
  @Override
  public List<FollowResponse> getFollowers(UUID userId) {
    List<UUID> list = followRepository.findAllByFollowingId(userId).stream().map(Follow::getFollowerId).toList();
    return list.stream().map(id -> FollowResponse.fromUserResponse(id, userService.find(id))).toList();
  }

  @Override
  public boolean isFollowing(UUID followerId, UUID followingId) {
    return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
  }

}
