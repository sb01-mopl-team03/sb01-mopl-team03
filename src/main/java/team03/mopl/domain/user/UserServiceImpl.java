package team03.mopl.domain.user;

import static team03.mopl.domain.user.Role.*;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.DuplicatedEmailException;
import team03.mopl.common.exception.user.DuplicatedNameException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.oauth2.GoogleUserInfo;
import team03.mopl.domain.oauth2.KakaoUserInfo;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public UserResponse create(UserCreateRequest request) {
    if(userRepository.existsByEmail(request.email())){
      throw new DuplicatedEmailException();
    }
    if (userRepository.existsByName(request.name())){
      throw new DuplicatedNameException();
    }
    User user = User.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .role(USER)
        .isLocked(false)
        .isTempPassword(false)
        .profileImage(request.profileImage())
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
    if(request.newPassword()!=null) {
      String encodedPassword = passwordEncoder.encode(request.newPassword());
      user.update(request.newName(),encodedPassword);
    }
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

  @Override
  public User loginOrRegisterOAuth(String email,String name){
    return userRepository.findByEmail(email)
        .orElseGet(()->{
          String randomPassword  = UUID.randomUUID().toString();
          return userRepository.save(User.builder()
              .email(email)
              .name(name)
              .password(passwordEncoder.encode(randomPassword ))
              .role(USER)
              .isLocked(false)
              .build());
        });
  }
}
