package team03.mopl.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 응답 DTO")
public record UserResponse(
    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String name,

    @Schema(description = "역할", example = "USER")
    String role,

    @Schema(description = "계정 잠김 여부", example = "false")
    boolean isLocked,

    @Schema(description = "프로필 이미지 경로")
    String profileImage) {
  public static UserResponse from(User user) {
    return new UserResponse(user.getEmail(), user.getName(), user.getRole().name(), user.isLocked(), user.getProfileImage());
  }
}



