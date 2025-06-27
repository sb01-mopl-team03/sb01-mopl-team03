package team03.mopl.domain.review.dto;

import java.math.BigDecimal;
import team03.mopl.domain.review.entity.Review;

public record ReviewResponse(
    String title,
    String comment,
    BigDecimal rating
) {

  public static ReviewResponse from(Review review) {
    return new ReviewResponse(review.getTitle(),
        review.getComment(), review.getRating());
  }
}
