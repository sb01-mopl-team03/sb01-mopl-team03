package team03.mopl.domain.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record UserUpdateRequest(
    @Size(min = 1, max = 20,message = "1글자 이상 20자 이하입니다.")
    String newName,
    @NotBlank(message = "비밀번호는 필수입니다.")
    //@Size(min = 8, max = 60, message = "비밀번호는 8자이상 60자 이하입니다.")
    //@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
    //    message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
    String newPassword
)
{
}
