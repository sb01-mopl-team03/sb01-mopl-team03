package team03.mopl.domain.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "리뷰 생성 요청")
public record ReviewCreateRequest(

    @Schema(description = "작성자 ID", example = "a1b2c3d4-1234-5678-9012-abcdefabcdef")
    @NotNull(message = "사용자 ID는 필수입니다")
    UUID userId,

    @Schema(description = "콘텐츠 ID", example = "c1d2e3f4-5678-1234-9012-fedcbafedcba")
    @NotNull(message = "콘텐츠 ID는 필수입니다")
    UUID contentId,

    @Schema(description = "리뷰 제목", example = "정말 감동적인 영화!")
    @NotBlank(message = "제목은 필수입니다")
    String title,

    @Schema(description = "리뷰 본문", example = "눈물이 멈추지 않았어요.")
    String comment,

    @Schema(description = "별점 (0.0 ~ 5.0, 소수점 1자리까지)", example = "4.5")
    @NotNull(message = "별점은 필수입니다")
    @DecimalMin(value = "0.0", message = "별점은 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다")
    @Digits(integer = 1, fraction = 1, message = "소수점 한자리까지만 입력 가능합니다")
    BigDecimal rating
) {}
