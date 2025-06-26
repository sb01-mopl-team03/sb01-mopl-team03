package team03.mopl.domain.review.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;

public record ReviewUpdateRequest(

    @NotBlank(message = "제목은 필수입니다.")
    String title,

    String comment,

    @NotBlank(message = "별점은 필수입니다")
    @DecimalMin(value = "0.0", message = "별점은 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다")
    @Digits(integer = 1, fraction = 1, message = "소수점 한자리까지만 입력 가능합니다")
    float rating
) {

}
