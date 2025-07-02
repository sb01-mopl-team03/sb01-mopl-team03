package team03.mopl.domain.follow.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.domain.user.UserResponse;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

  private final FollowService followService;

  // 팔로우하기
  @PostMapping("/{followerId}/follow/{followingId}")
  public ResponseEntity<Void> follow(@PathVariable UUID followerId, @PathVariable UUID followingId) {
    followService.follow(followerId, followingId);
    return ResponseEntity.ok().build();
  }
  @DeleteMapping("/{followerId}/unfollow/{followingId}")
  public ResponseEntity<Void> unfollow(
      @PathVariable UUID followerId,
      @PathVariable UUID followingId
  ) {
    followService.unfollow(followerId, followingId);
    return ResponseEntity.ok().build();
  }
  // 팔로잉 목록
  @GetMapping("/{userId}/following")
  public ResponseEntity<List<UserResponse>> getFollowing(
      @PathVariable UUID userId
  ) {
    return ResponseEntity.ok(followService.getFollowing(userId));
  }

  // 팔로워 목록
  @GetMapping("/{userId}/followers")
  public ResponseEntity<List<UserResponse>> getFollowers(
      @PathVariable UUID userId
  ) {
    return ResponseEntity.ok(followService.getFollowers(userId));
  }

  // 팔로우 여부 확인
  @GetMapping("/{followerId}/is-following/{followingId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable UUID followerId,
      @PathVariable UUID followingId
  ) {
    return ResponseEntity.ok(followService.isFollowing(followerId, followingId));
  }
}
