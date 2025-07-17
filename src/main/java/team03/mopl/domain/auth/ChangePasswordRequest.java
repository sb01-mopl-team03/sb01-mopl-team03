package team03.mopl.domain.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


@Schema(description = "비밀번호 변경 요청 DTO")
public record ChangePasswordRequest(

    @Schema(description = "새 비밀번호", example = "newSecurePass123!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    //@Size(min = 8, max = 60, message = "비밀번호는 8자이상 60자 이하입니다.")
    //@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
    //    message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
    String newPassword
) {
}
