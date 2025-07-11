package team03.mopl.domain.review.dto;

import java.math.BigDecimal;
import java.util.UUID;
import team03.mopl.domain.review.entity.Review;

public record ReviewDto(
    UUID id,
    UUID authorId,
    String authorName,
    String title,
    String comment,
    BigDecimal rating
) {

  public static ReviewDto from(Review review) {
    return new ReviewDto(
        review.getId(),
        review.getUser().getId(),
        review.getUser().getName(),
        review.getTitle(),
        review.getComment(),
        review.getRating());
  }
}
