package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import team03.mopl.domain.user.*;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.subscription.dto.SubscriptionDto;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.playlist.dto.PlaylistDto;

@Tag(name = "User API", description = "사용자 관리 및 사용자 관련 정보 조회 API")
@RequestMapping("/api/users")
public interface UserApi {

  @Operation(summary = "회원가입", description = "사용자 정보와 프로필 이미지를 함께 전송하여 회원가입을 진행합니다.")
  @ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = UserResponse.class)))
  @PostMapping(consumes = "multipart/form-data")
  ResponseEntity<UserResponse> create(@ModelAttribute UserCreateRequest request);

  @Operation(summary = "회원 단건 조회", description = "사용자 ID로 사용자 정보를 조회합니다.")
  @GetMapping("/{userId}")
  ResponseEntity<UserResponse> find(@PathVariable UUID userId);

  @Operation(summary = "회원 정보 수정", description = "사용자 정보를 수정합니다. 프로필 이미지를 함께 수정할 수 있습니다.")
  @PutMapping(value = "/{userId}", consumes = "multipart/form-data")
  ResponseEntity<UserResponse> update(
      @PathVariable UUID userId,
      @RequestPart UserUpdateRequest request,
      @RequestPart(value = "profile", required = false) MultipartFile profile
  );

  @Operation(summary = "회원 탈퇴", description = "사용자 계정을 삭제합니다.")
  @DeleteMapping("/{userId}")
  ResponseEntity<Void> delete(@PathVariable UUID userId);

  @Operation(summary = "전체 사용자 조회")
  @GetMapping
  ResponseEntity<List<UserResponse>> findAll();

  @Operation(summary = "사용자 리뷰 목록 조회")
  @GetMapping("/{userId}/reviews")
  ResponseEntity<List<ReviewDto>> getAllReviewByUser(@PathVariable UUID userId);

  @Operation(summary = "사용자 키워드 목록 조회")
  @GetMapping("/{userId}/keywords")
  ResponseEntity<List<KeywordDto>> getAllKeywordsByUser(@PathVariable UUID userId);

  @Operation(summary = "사용자 재생목록 목록 조회")
  @GetMapping("/{userId}/playlists")
  ResponseEntity<List<PlaylistDto>> getAllPlaylistByUser(@PathVariable UUID userId);

  @Operation(summary = "사용자 구독 채널 목록 조회")
  @GetMapping("/{userId}/subscriptions")
  ResponseEntity<List<SubscriptionDto>> getUserSubscriptions(@PathVariable UUID userId);
}
