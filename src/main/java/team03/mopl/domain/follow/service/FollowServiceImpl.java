package team03.mopl.domain.follow.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.domain.follow.entity.Follow;
import team03.mopl.domain.follow.repository.FollowRepository;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.User;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {
  private final FollowRepository followRepository;
  private final UserRepository userRepository;

  @Override
  public void follow(UUID followerId, UUID followingId) {
    User follower = userRepository.findById(followerId).orElseThrow();
    User following = userRepository.findById(followingId).orElseThrow();
    if( !followingId.equals(followerId) ) {
      throw new IllegalArgumentException(); //자신을 팔로잉할 수 없음
    }
    boolean alreadyFollowing = followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    if( alreadyFollowing ) {
      throw new IllegalArgumentException(); //이미 팔로잉 중
    }
    Follow follow = new Follow(followerId, followingId);
    followRepository.save(follow);
  }

  @Override
  public void unfollow(UUID followerId, UUID followingId) {
    User follower = userRepository.findById(followerId).orElseThrow();
    User following = userRepository.findById(followingId).orElseThrow();
    followRepository.deleteByFollowerIdAndFollowingId(followerId, followingId);
  }
  /*
  * 
  * UserDto 받고나서 반환 값 List<UUID> -> List<UserDto> 로 수정하기
  * 
  * */
  //나의 팔로잉 목록
  @Override
  public List<UUID> getFollowing(UUID userId) {
    return followRepository.findAllByFollowerId(userId).stream().map(Follow::getFollowingId).toList();
  }
  //나를 팔로우하는 사람들 목록
  @Override
  public List<UUID> getFollowers(UUID userId) {
    return followRepository.findAllByFollowingId(userId).stream().map(Follow::getFollowerId).toList();
  }

  @Override
  public boolean isFollowing(UUID followerId, UUID followingId) {
    return followRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
  }

}
