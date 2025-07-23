package team03.mopl.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "사용자 수정 DTO")
public record UserUpdateRequest(

    @Schema(description = "변경할 이름", example = "모플개발자")
    @Size(min = 1, max = 20,message = "1글자 이상 20자 이하입니다.")
    String newName,

    @Schema(description = "현재 비밀번호 (변경 확인용)", example = "mopl1234!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 60, message = "비밀번호는 8자이상 60자 이하입니다.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
        message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
    String newPassword
)
{
}
