package team03.mopl.domain.review.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.service.ReviewService;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

  private final ReviewService reviewService;

  @PostMapping
  public ResponseEntity<ReviewResponse> create(@RequestBody ReviewCreateRequest request) {
    return ResponseEntity.ok(reviewService.create(request));
  }

  @GetMapping("/{reviewId}")
  public ResponseEntity<ReviewResponse> get(@PathVariable UUID reviewId) {
    return ResponseEntity.ok(reviewService.get(reviewId));
  }

  @PatchMapping("/{reviewId}")
  public ResponseEntity<ReviewResponse> update(@PathVariable UUID reviewId, @RequestBody
      ReviewUpdateRequest request) {
    return ResponseEntity.ok(reviewService.update(reviewId, request));
  }

  @DeleteMapping("/{reviewId}")
  public ResponseEntity<Void> delete(@PathVariable UUID reviewId) {
    reviewService.delete(reviewId);
    return ResponseEntity.noContent().build();
  }


}
