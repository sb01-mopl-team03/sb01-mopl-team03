package team03.mopl.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.review.entity.Review;


@Schema(description = "리뷰 응답 DTO")
public record ReviewDto(
    @Schema(description = "리뷰 ID", example = "1f2e3d4c-1234-5678-9abc-def123456789")
    UUID id,

    @Schema(description = "작성자 ID", example = "u1234567-aaaa-bbbb-cccc-111122223333")
    UUID authorId,

    @Schema(description = "작성자 이름", example = "홍길동")
    String authorName,

    @Schema(description = "리뷰 제목", example = "완전 추천합니다")
    String title,

    @Schema(description = "리뷰 본문", example = "처음부터 끝까지 몰입해서 봤습니다.")
    String comment,

    @Schema(description = "작성 일시", example = "2025-07-16T10:45:00")
    LocalDateTime createdAt,

    @Schema(description = "별점", example = "4.5")
    BigDecimal rating
) {

  public static ReviewDto from(Review review) {
    return new ReviewDto(
        review.getId(),
        review.getUser().getId(),
        review.getUser().getName(),
        review.getTitle(),
        review.getComment(),
        review.getCreatedAt(),
        review.getRating());
  }
}
