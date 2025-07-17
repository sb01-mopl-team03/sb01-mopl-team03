package team03.mopl.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "리뷰 수정 요청")
public record ReviewUpdateRequest(

    @Schema(description = "수정할 리뷰 제목", example = "조금 아쉬웠던 점도 있었어요.")
    @NotBlank(message = "제목은 필수입니다.")
    String title,

    @Schema(description = "수정할 리뷰 본문", example = "스토리는 좋았지만 후반부가 다소 아쉬웠습니다.")
    String comment,

    @Schema(description = "수정할 별점 (0.0 ~ 5.0)", example = "3.5")
    @NotNull(message = "별점은 필수입니다")
    @DecimalMin(value = "0.0", message = "별점은 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다")
    @Digits(integer = 1, fraction = 1, message = "소수점 한자리까지만 입력 가능합니다")
    BigDecimal rating
) {}
