package team03.mopl.domain.follow.dto;


import java.util.UUID;
import team03.mopl.domain.user.UserResponse;

public record FollowResponse(
    UUID id,
    String email,
    String name,
    String role,
    String profileImage
) {

  public static FollowResponse fromUserResponse(UUID id, UserResponse userResponse) {
    return new FollowResponse(id,
        userResponse.email(),
        userResponse.name(),
        userResponse.role(),
        userResponse.profileImage());
  }
}
