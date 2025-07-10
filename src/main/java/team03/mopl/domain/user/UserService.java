package team03.mopl.domain.user;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;


public interface UserService {
  UserResponse create(UserCreateRequest request);
  UserResponse find(UUID userId);
  UserResponse update(UUID userId, UserUpdateRequest request, MultipartFile profile);
  void delete(UUID userId);
  List<UserResponse> findAll();
  User loginOrRegisterOAuth(String email,String name);
}
