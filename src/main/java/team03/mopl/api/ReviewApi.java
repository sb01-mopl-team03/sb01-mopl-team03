package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "Review API", description = "리뷰 작성 및 수정, 삭제 API")
@RequestMapping("/api/reviews")
public interface ReviewApi {

  @Operation(summary = "리뷰 생성", description = "리뷰를 작성합니다.")
  @ApiResponse(responseCode = "200", description = "작성 성공",
      content = @Content(schema = @Schema(implementation = ReviewDto.class)))
  @PostMapping
  ResponseEntity<ReviewDto> create(@RequestBody ReviewCreateRequest request);

  @Operation(summary = "리뷰 단건 조회", description = "리뷰 ID로 리뷰를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = ReviewDto.class)))
  @GetMapping("/{reviewId}")
  ResponseEntity<ReviewDto> get(@PathVariable UUID reviewId);

  @Operation(summary = "리뷰 수정", description = "본인이 작성한 리뷰를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공", content = @Content(schema = @Schema(implementation = ReviewDto.class))),
      @ApiResponse(responseCode = "403", description = "수정 권한 없음")
  })
  @PatchMapping("/{reviewId}")
  ResponseEntity<ReviewDto> update(
      @PathVariable UUID reviewId,
      @RequestBody ReviewUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "리뷰 삭제", description = "본인이 작성한 리뷰를 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "삭제 권한 없음")
  })
  @DeleteMapping("/{reviewId}")
  ResponseEntity<Void> delete(
      @PathVariable UUID reviewId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
