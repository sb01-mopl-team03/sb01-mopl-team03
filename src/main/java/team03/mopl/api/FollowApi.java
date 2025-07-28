package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import team03.mopl.domain.follow.dto.FollowResponse;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "Follow API", description = "팔로우 기능 관련 API")
@RequestMapping("/api/follows")
public interface FollowApi {

  @Operation(summary = "팔로우", description = "사용자가 다른 사용자를 팔로우합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "팔로우 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping("/{followingId}")
  ResponseEntity<Void> follow(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("followingId") String followingId);

  @Operation(summary = "언팔로우", description = "사용자가 팔로우를 취소합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "언팔로우 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @DeleteMapping("/{followingId}")
  ResponseEntity<Void> unfollow(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable("followingId") String followingId);

  @Operation(summary = "팔로잉 목록 조회", description = "지정한 사용자가 팔로우하고 있는 사용자 목록을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = FollowResponse.class)))
  @GetMapping("/{userId}/following")
  ResponseEntity<List<FollowResponse>> getFollowing(
      @Parameter(description = "사용자 ID") @PathVariable UUID userId
  );

  @Operation(summary = "팔로워 목록 조회", description = "지정한 사용자를 팔로우하는 사용자 목록을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = FollowResponse.class)))
  @GetMapping("/{userId}/followers")
  ResponseEntity<List<FollowResponse>> getFollowers(
      @Parameter(description = "사용자 ID") @PathVariable UUID userId
  );

  @Operation(summary = "팔로우 여부 확인", description = "특정 사용자가 다른 사용자를 팔로우하고 있는지 여부를 확인합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = Boolean.class))),
      @ApiResponse(responseCode = "404", description = "사용자 없음")
  })
  @GetMapping("/{followerId}/is-following/{followingId}")
  ResponseEntity<Boolean> isFollowing(
      @Parameter(description = "팔로우 하는 사용자 ID") @PathVariable UUID followerId,
      @Parameter(description = "팔로우 당하는 사용자 ID") @PathVariable UUID followingId
  );
}
