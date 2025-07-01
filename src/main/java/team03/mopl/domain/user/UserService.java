package team03.mopl.domain.user;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.oauth2.GoogleUserInfo;

public interface UserService {
  UserResponse create(UserCreateRequest request);
  UserResponse find(UUID userId);
  UserResponse update(UUID userId, UserUpdateRequest request);
  void delete(UUID userId);
  List<UserResponse> findAll();
  User loginOrRegisterByGoogle(GoogleUserInfo googleUser);
}
