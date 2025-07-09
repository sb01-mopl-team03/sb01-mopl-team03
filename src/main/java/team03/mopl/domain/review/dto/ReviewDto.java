package team03.mopl.domain.review.dto;

import java.math.BigDecimal;
import team03.mopl.domain.review.entity.Review;

public record ReviewDto(
    String title,
    String comment,
    BigDecimal rating
) {

  public static ReviewDto from(Review review) {
    return new ReviewDto(review.getTitle(),
        review.getComment(), review.getRating());
  }
}
