package team03.mopl.domain.user;

public record UserResponse(String email,String name,String role, boolean isLocked, String profileImage) {
  public static UserResponse from(User user) {
    return new UserResponse(user.getEmail(), user.getName(), user.getRole().name(), user.isLocked(), user.getProfileImage());
  }
}



