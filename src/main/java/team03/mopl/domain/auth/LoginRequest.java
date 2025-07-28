package team03.mopl.domain.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public record LoginRequest(

    @Schema(description = "사용자 이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email
    String email,

    @Schema(description = "비밀번호", example = "mopl1234!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password
) {
}
