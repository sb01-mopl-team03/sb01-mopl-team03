package team03.mopl.domain.follow.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.user.UserResponse;

public interface FollowService {
  void follow(UUID followerId, UUID followingId);
  void unfollow(UUID followerId, UUID followingId);
  void deletedUserUnfollow(UUID userId);
  List<FollowResponse> getFollowing(UUID userId);   // 내가 팔로우한 사람 목록
  List<FollowResponse> getFollowers(UUID userId);   // 나를 팔로우한 사람 목록
  boolean isFollowing(UUID followerId, UUID followingId);
}

