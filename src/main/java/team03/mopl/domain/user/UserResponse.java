package team03.mopl.domain.user;

public record UserResponse(String email,String name,String role, boolean isLocked) {
  public static UserResponse from(User user) {
    return new UserResponse(user.getEmail(), user.getName(), user.getRole().name(), user.isLocked());
  }
}
