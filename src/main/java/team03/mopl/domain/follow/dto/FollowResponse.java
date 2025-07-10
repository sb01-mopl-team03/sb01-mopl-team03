package team03.mopl.domain.follow.dto;


import java.util.UUID;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserResponse;

public record FollowResponse(
    UUID id,
    String email,
    String name,
    String role,
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
