package team03.mopl.domain.follow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import team03.mopl.domain.user.User;

@Schema(description = "팔로우 응답 DTO")
public record FollowResponse(
    @Schema(description = "사용자 ID", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    UUID id,

    @Schema(description = "이메일", example = "user@example.com")
    String email,

    @Schema(description = "이름", example = "홍길동")
    String name,

    @Schema(description = "역할", example = "USER")
    String role,

    @Schema(description = "프로필 이미지 URL", example = "/static/profile/default.png")
    String profileImage
) {
  public static FollowResponse fromUser(User user) {
    return new FollowResponse(
        user.getId(),
        user.getEmail(),
        user.getName(),
        user.getRole().toString(),
        user.getProfileImage()
    );
  }
}
