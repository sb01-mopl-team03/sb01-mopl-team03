package team03.mopl.domain.review.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewCreateRequest request) {
    return ResponseEntity.ok(reviewService.create(request));
  }

  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> get(@PathVariable UUID reviewId) {
    return ResponseEntity.ok(reviewService.get(reviewId));
  }

  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewDto> update(@PathVariable UUID reviewId,
      @Valid @RequestBody ReviewUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();
    return ResponseEntity.ok(reviewService.update(reviewId, request, userId));
  }

  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID reviewId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();
    reviewService.delete(reviewId, userId);
    return ResponseEntity.noContent().build();
  }
}
