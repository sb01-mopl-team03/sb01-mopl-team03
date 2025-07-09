package team03.mopl.domain.review.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.review.entity.Review;

public record ReviewResponse(
    UUID id,
    UUID authorId,
    String authorName,
    String title,
    String comment,
    LocalDateTime createdAt,
    BigDecimal rating
) {

  public static ReviewResponse from(Review review) {
    return new ReviewResponse(
        review.getId(),
        review.getUser().getId(),
        review.getUser().getName(),
        review.getTitle(),
        review.getComment(),
        review.getCreatedAt(),
        review.getRating());
  }
}
