package team03.mopl.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "사용자 생성 DTO")
public record UserCreateRequest(

    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    String email,

    @Schema(description = "이름", example = "홍길동")
    @NotBlank(message = "사용자 이름은 필수 입니다.")
    @Size(min = 3, max = 50, message = "사용자 이름은 3자이상 50자 이하입니다.")
    String name,

    @Schema(description = "비밀번호", example = "mopl1234!")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 60, message = "비밀번호는 8자이상 60자 이하입니다.")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
        message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
    String password,

    @Schema(description = "프로필 이미지 파일 (선택)", type = "string", format = "binary")
    MultipartFile profile
) {

}
