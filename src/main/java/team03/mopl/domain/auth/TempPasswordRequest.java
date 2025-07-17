package team03.mopl.domain.auth;


import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "임시 비밀번호 요청 DTO")
public record TempPasswordRequest(

    @Schema(description = "요청자 이메일", example = "user@example.com")
    String email
) {

}
