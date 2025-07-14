package team03.mopl.domain.user;

import static team03.mopl.domain.user.Role.*;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import team03.mopl.common.exception.user.DuplicatedEmailException;
import team03.mopl.common.exception.user.DuplicatedNameException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.storage.ProfileImageStorage;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final FollowService followService;
  private final ProfileImageStorage profileImageStorage;

  @PostConstruct
  public void logStorageType() {
    log.info("ProfileImageStorage : {}", profileImageStorage.getClass().getSimpleName());
  }

  @Override
  public UserResponse create(UserCreateRequest request) {
    if(userRepository.existsByEmail(request.email())){
      throw new DuplicatedEmailException();
    }
    if (userRepository.existsByName(request.name())){
      throw new DuplicatedNameException();
    }

    String uploadedImageUrl = null;
    if (request.profile() != null && !request.profile().isEmpty()){
      uploadedImageUrl = profileImageStorage.upload(request.profile());
    }else{
      uploadedImageUrl = "/static/profile/woody.png"; //기본 이미지
    }

    User user = User.builder()
        .name(request.name())
        .email(request.email())
        .password(passwordEncoder.encode(request.password()))
        .role(USER)
        .isLocked(false)
        .isTempPassword(false)
        .profileImage(uploadedImageUrl)
        .build();
    return UserResponse.from(userRepository.save(user));
  }

  @Override
  public UserResponse find(UUID userId) {
    return UserResponse.from(userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new));
  }

  @Override
  @Transactional
  public UserResponse update(UUID userId, UserUpdateRequest request, MultipartFile profile) {
    User user= userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    String encodedPassword = null;
    if(request.newPassword() != null){
      encodedPassword = passwordEncoder.encode(request.newPassword());
    }

    String newImageUrl = null;
    if(profile != null && !profile.isEmpty()){
      if(user.getProfileImage() != null && !user.getProfileImage().startsWith("/static/profile")){
        profileImageStorage.delete(user.getProfileImage());
      }
      newImageUrl = profileImageStorage.upload(profile);
    }else{
      newImageUrl = user.getProfileImage();
    }

    user.update(request.newName(), encodedPassword, newImageUrl);

    return UserResponse.from(user);
  }

  @Override
  public void delete(UUID userId) {
    // User 삭제 시 Follow 관계도 같이 삭제
    followService.deletedUserUnfollow(userId);

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
