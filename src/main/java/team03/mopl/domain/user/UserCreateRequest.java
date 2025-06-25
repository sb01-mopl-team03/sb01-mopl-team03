package team03.mopl.domain.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    String email,

    @NotBlank(message = "사용자 이름은 필수 입니다.")
    @Size(min = 3, max = 50, message = "사용자 이름은 3자이상 50자 이하입니다.")
    String name,

    @NotBlank(message = "비밀번호는 필수입니다.")
    //@Size(min = 8, max = 60, message = "비밀번호는 8자이상 60자 이하입니다.")
    //@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
    //    message = "비밀번호는 최소 8자 이상, 숫자, 문자, 특수문자를 포함해야 합니다")
    String password) {

}
