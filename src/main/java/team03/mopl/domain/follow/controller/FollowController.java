package team03.mopl.domain.follow.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.common.exception.follow.BadRequestFollowingException;
import team03.mopl.api.FollowApi;
import team03.mopl.domain.follow.dto.FollowRequest;
import team03.mopl.domain.follow.dto.FollowResponse;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Slf4j
public class FollowController implements FollowApi {

  private final FollowService followService;

  @Override
  @PostMapping("/follow")
  public ResponseEntity<Void> follow( @AuthenticationPrincipal CustomUserDetails user, @RequestBody FollowRequest request) {
    //로그인된 사람과 팔로우하려는 사람과 같은지 확인
    if( user.getId() != request.getFollowerId() ) {
      throw new BadRequestFollowingException();
    }
    followService.follow(request.getFollowerId(), request.getFollowingId());
    return ResponseEntity.ok().build();
  }

  @Override
  @DeleteMapping("/unfollow")
  public ResponseEntity<Void> unfollow(@AuthenticationPrincipal CustomUserDetails user, @RequestBody FollowRequest request) {
    //로그인된 사람과 언팔하려는 사람 같은지 확인
    if( user.getId() != request.getFollowerId() ) {
      throw new BadRequestFollowingException();
    }
    followService.unfollow(request.getFollowerId(), request.getFollowingId());
    return ResponseEntity.ok().build();
  }

  // 팔로잉 목록
  @Override
  @GetMapping("/{userId}/following")
  public ResponseEntity<List<FollowResponse>> getFollowing(
      @PathVariable(name = "userId") UUID userId
  ) {
    return ResponseEntity.ok(followService.getFollowing(userId));
  }

  // 팔로워 목록
  @Override
  @GetMapping("/{userId}/followers")
  public ResponseEntity<List<FollowResponse>> getFollowers(
      @PathVariable(name = "userId") UUID userId
  ) {
    return ResponseEntity.ok(followService.getFollowers(userId));
  }

  // 팔로우 여부 확인
  @Override
  @GetMapping("/{followerId}/is-following/{followingId}")
  public ResponseEntity<Boolean> isFollowing(
      @PathVariable(name = "followerId") UUID followerId, @PathVariable(name = "followingId") UUID followingId
  ) {
    return ResponseEntity.ok(followService.isFollowing(followerId, followingId));
  }
}
