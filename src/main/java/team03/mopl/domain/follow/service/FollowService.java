package team03.mopl.domain.follow.service;

import java.util.List;
import java.util.UUID;

public interface FollowService {
  void follow(UUID followerId, UUID followingId);
  void unfollow(UUID followerId, UUID followingId);
  List<UUID> getFollowing(UUID userId);   // 내가 팔로우한 사람 목록
  List<UUID> getFollowers(UUID userId);   // 나를 팔로우한 사람 목록
  boolean isFollowing(UUID followerId, UUID followingId);
}

