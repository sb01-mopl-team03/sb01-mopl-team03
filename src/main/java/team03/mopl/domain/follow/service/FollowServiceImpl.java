package team03.mopl.domain.follow.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.follow.AlreadyFollowingException;
import team03.mopl.common.exception.follow.CantFollowSelfException;
import team03.mopl.common.exception.follow.FollowNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.follow.dto.FollowResponse;
import team03.mopl.domain.follow.entity.Follow;
import team03.mopl.domain.follow.repository.FollowRepository;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserService;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public void follow(UUID followerId, UUID followingId) {
    log.info("follow - 팔로우 시도: followerId={}, followingId={}", followerId, followingId);
    //팔로우 하는 사람
    User follower = userRepository.findById(followerId).orElseThrow(() -> {
      log.warn("follow - 존재하지 않는 팔로워: followerId={}", followerId);
      return new UserNotFoundException();
    });
    //팔로우 당하는 사람
    User following = userRepository.findById(followingId).orElseThrow(() -> {
      log.warn("follow - 존재하지 않는 팔로잉 대상: followingId={}", followingId);
      return new UserNotFoundException();
    });
    if (followingId.equals(followerId)) {
      log.warn("follow - 자기 자신을 팔로우 시도: userId={}", followerId);
      throw new CantFollowSelfException(); //자신을 팔로잉할 수 없음
    }
    boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    if (alreadyFollowing) {
      log.warn("follow - 해당 유저를 이미 팔로우 중: followerId={} -> followingId={}", followerId, followingId);
      throw new AlreadyFollowingException(); //이미 팔로잉 중
    }
    Follow follow = new Follow(followerId, followingId);
    followRepository.save(follow);

    // 알림 전송 추가
    notificationService.sendNotification(
        new NotificationDto(following.getId(), NotificationType.FOLLOWED,
            follower.getName()+ "이(가) "+following.getName()+"를 팔로우 했습니다.", false));
    log.info("follow - 팔로우 성공: followerId={}, followingId={}", followerId, followingId);
  }

  @Override
  @Transactional
  public void unfollow(UUID unfollowerId, UUID unfollowingId) {
    log.info("unfollow - 언팔로우 시도: unfollowerId={}, unfollowingId={}", unfollowerId, unfollowingId);
    User unFollower = userRepository.findById(unfollowerId).orElseThrow(() -> {
      log.warn("unfollow - 언팔로우 실패 - 존재하지 않는 사용자(unfollowerId): {}", unfollowerId);
      return new UserNotFoundException();
    });
    User unFollowing = userRepository.findById(unfollowingId).orElseThrow(() -> {
      log.warn("unfollow - 언팔로우 실패 - 존재하지 않는 사용자(unfollowingId): {}", unfollowingId);
      return new UserNotFoundException();
    });

    //팔로우 관계 확인
    if (isFollowing(unfollowerId, unfollowingId)) {
      //삭제
      followRepository.deleteByFollowerIdAndFollowingId(unfollowerId, unfollowingId);
      // 알림 전송 추가
      notificationService.sendNotification(
          new NotificationDto(unFollowing.getId(), NotificationType.UNFOLLOWED,
              unFollower.getName()+" 이(가) "+unFollowing.getName() + "을(를) 언팔로우 했습니다.", false));
      log.info("unfollow - 언팔로우 성공: unfollowerId={}, unfollowingId={}", unfollowerId, unfollowingId);

    } else {
      log.warn("unfollow - 언팔로우 실패 - 팔로우 관계가 없음: unfollowerId={}, unfollowingId={}", unfollowerId, unfollowingId);
      throw new FollowNotFoundException();
    }
  }

  @Override
  public void deletedUserUnfollow(UUID userId) {
    //기존 팔로우, 팔로잉한 user가 삭제된 상태
    //해당 유저를 팔로우, 팔로잉한 follow 객체 삭제

    // 내가 다른 사람을 팔로우했던 기록 다 삭제
    log.info("deletedUserUnfollow - 회원 탈퇴에 따른 팔로우/팔로잉 관계 정리: userId={}", userId);
    followRepository.deleteByFollowerId(userId);
    // 나를 팔로우했던 기록도 다 삭제
    followRepository.deleteByFollowingId(userId);
    log.info("deletedUserUnfollow - 회원 탈퇴 관계 정리 완료: userId={}", userId);
  }

  //나의 팔로잉 목록
  @Override
  public List<FollowResponse> getFollowing(UUID userId) {
    log.info("getFollowing - 팔로잉 목록 조회: userId={}", userId);

    List<UUID> list = followRepository.findAllByFollowerId(userId).stream().map(Follow::getFollowingId).toList();

    return list.stream().map(id -> FollowResponse.fromUser(userRepository.findById(id).orElseThrow(UserNotFoundException::new))).toList();
  }

  //나를 팔로우하는 사람들 목록
  @Override
  public List<FollowResponse> getFollowers(UUID userId) {
    log.info("getFollowers - 팔로워 목록 조회: userId={}", userId);

    List<UUID> list = followRepository.findAllByFollowingId(userId).stream().map(Follow::getFollowerId).toList();

    return list.stream().map(id -> FollowResponse.fromUser(userRepository.findById(id).orElseThrow(UserNotFoundException::new))).toList();
  }

  @Override
  public boolean isFollowing(UUID followerId, UUID followingId) {
    log.info("isFollowing - 팔로우 여부 확인: followerId={}, followingId={}", followerId, followingId);
    return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
  }

}
