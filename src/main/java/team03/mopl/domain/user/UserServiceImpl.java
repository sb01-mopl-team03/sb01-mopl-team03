package team03.mopl.domain.user;

import static team03.mopl.domain.user.Role.*;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.UserNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserResponse create(UserCreateRequest request) {
    User user = User.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .role(USER)
        .build();
    return UserResponse.from(userRepository.save(user));
  }

  @Override
  public UserResponse find(UUID userId) {
    return UserResponse.from(userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new));
  }

  @Override
  public UserResponse update(UUID userId, UserUpdateRequest request) {
    User user= userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    user.setPassword(passwordEncoder.encode(request.password()));
    return UserResponse.from(user);
  }

  @Override
  public void delete(UUID userId) {
    userRepository.deleteById(userId);
  }

  @Override
  public List<UserResponse> findAll() {
    return userRepository.findAll().stream().map(UserResponse::from).toList();
  }
}
