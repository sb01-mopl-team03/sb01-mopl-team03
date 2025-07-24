package team03.mopl.domain.user;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import team03.mopl.common.exception.auth.InvalidPasswordException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.storage.ProfileImageStorage;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private ProfileImageStorage profileImageStorage;

  @Mock
  private FollowService followService;

  @InjectMocks
  private UserServiceImpl userService;

  private UserCreateRequest userCreateRequest;

  @BeforeEach
  void setUp() {
    userCreateRequest = new UserCreateRequest("test@email.com", "test", "test", null);
  }


  @Test
  @DisplayName("유저 생성 성공")
  void createUser() {
    // given
    given(passwordEncoder.encode("test")).willReturn("encoded-password");

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

    // when
    UserResponse response = userService.create(userCreateRequest);

    // then
    then(userRepository).should().save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.getEmail()).isEqualTo("test@email.com");
    assertThat(savedUser.getPassword()).isEqualTo("encoded-password");

    assertThat(response.email()).isEqualTo("test@email.com");
    assertThat(response.name()).isEqualTo("test");
  }

  @Test
  @DisplayName("유저 조회 성공")
  void findUser() {
    // given
    User user = User.builder()
        .name("홍길동")
        .email("hong@test.com")
        .password("encoded")
        .role(Role.USER)
        .build();
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    UserResponse response = userService.find(user.getId());

    // then
    assertThat(response.email()).isEqualTo("hong@test.com");
  }

  @Test
  @DisplayName("유저 조회 실패 - 없는 ID")
  void findUser_notFound() {
    // given
    UUID notExistId = UUID.randomUUID();
    given(userRepository.findById(notExistId)).willReturn(Optional.empty());

    // expect
    assertThrows(UserNotFoundException.class, () -> userService.find(notExistId));
  }

  @Test
  @DisplayName("유저 비밀번호 수정 - 현재 비밀번호 일치 시 성공")
  void updateUser_withCorrectCurrentPassword() {
    // given
    UUID id = UUID.randomUUID();
    User user = User.builder()
        .id(id)
        .email("test@test.com")
        .name("수정맨")
        .password("encoded-oldpass") // DB에 저장된 인코딩된 비번
        .role(Role.USER)
        .build();

    UserUpdateRequest request = new UserUpdateRequest("수정맨", "oldpass", "newpass");

    given(userRepository.findById(id)).willReturn(Optional.of(user));
    given(passwordEncoder.matches("oldpass", "encoded-oldpass")).willReturn(true);
    given(passwordEncoder.encode("newpass")).willReturn("encoded-newpass");

    // when
    UserResponse response = userService.update(id, request, null);

    // then
    assertThat(user.getPassword()).isEqualTo("encoded-newpass");
    assertThat(response.name()).isEqualTo("수정맨");
  }

  @Test
  @DisplayName("유저 비밀번호 수정 실패 - 현재 비밀번호 불일치")
  void updateUser_withWrongCurrentPassword() {
    UUID id = UUID.randomUUID();
    User user = User.builder()
        .id(id)
        .email("test@test.com")
        .name("수정맨")
        .password("encoded-oldpass")
        .role(Role.USER)
        .build();

    UserUpdateRequest request = new UserUpdateRequest("수정맨", "wrongpass", "newpass");

    given(userRepository.findById(id)).willReturn(Optional.of(user));
    given(passwordEncoder.matches("wrongpass", "encoded-oldpass")).willReturn(false);

    assertThrows(InvalidPasswordException.class, () -> userService.update(id, request, null));
  }


  @Test
  @DisplayName("유저 이름만 수정")
  void updateUserNameOnly() {
    // given
    UUID id = UUID.randomUUID();
    User user = User.builder()
        .id(id)
        .email("aaa@a.com")
        .name("수정맨")
        .password("old")
        .role(Role.USER)
        .profileImage("/static/profile/woody.png")
        .build();

    UserUpdateRequest request = new UserUpdateRequest("변경된이름", "old",null);
    given(userRepository.findById(id)).willReturn(Optional.of(user));
    given(passwordEncoder.matches("old", "old")).willReturn(true);

    // when
    UserResponse response = userService.update(id, request, null);

    // then
    assertThat(user.getName()).isEqualTo("변경된이름");
    assertThat(response.name()).isEqualTo("변경된이름");
  }


  @Test
  @DisplayName("유저 프로필 이미지 수정")
  void updateUserProfileImage() {
    // given
    UUID id = UUID.randomUUID();
    User user = User.builder()
        .id(id)
        .email("profile@test.com")
        .name("프로필")
        .password("pw")
        .role(Role.USER)
        .profileImage("/local/profile/old.png")
        .build();

    MultipartFile newProfile = mock(MultipartFile.class);
    given(newProfile.isEmpty()).willReturn(false);
    given(userRepository.findById(id)).willReturn(Optional.of(user));
    given(profileImageStorage.upload(newProfile)).willReturn("/local/profile/new.png");
    given(passwordEncoder.matches("pw", "pw")).willReturn(true);

    UserUpdateRequest request = new UserUpdateRequest(null, "pw",null);

    // when
    UserResponse response = userService.update(id, request, newProfile);

    // then
    assertThat(user.getProfileImage()).isEqualTo("/local/profile/new.png");
    assertThat(response.profileImage()).isEqualTo("/local/profile/new.png");

    then(profileImageStorage).should().delete("/local/profile/old.png");
  }

  @Test
  @DisplayName("유저 전체 조회")
  void findAllUsers() {
    // given
    User user1 = User.builder().email("a@a.com").name("A").password("pw").role(Role.USER).build();
    User user2 = User.builder().email("b@b.com").name("B").password("pw").role(Role.USER).build();

    given(userRepository.findAll()).willReturn(List.of(user1, user2));

    // when
    List<UserResponse> result = userService.findAll();

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).email()).isEqualTo("a@a.com");
  }

  @Test
  @DisplayName("유저 삭제")
  void deleteUser() {
    // given
    UUID id = UUID.randomUUID();

    // when
    userService.delete(id);

    // then
    then(userRepository).should().deleteById(id);
  }

  @Test
  @DisplayName("OAuth 로그인 - 기존 유저가 있으면 로그인 처리")
  void loginOrRegisterOAuth_existingUser() {
    String email = "oauth@test.com";
    String name = "OAuthUser";
    User existingUser = User.builder()
        .id(UUID.randomUUID())
        .email(email)
        .name(name)
        .password("encoded")
        .role(Role.USER)
        .build();

    given(userRepository.findByEmail(email)).willReturn(Optional.of(existingUser));

    User result = userService.loginOrRegisterOAuth(email, name);

    assertThat(result).isEqualTo(existingUser);
    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  @DisplayName("OAuth 로그인 - 유저가 없으면 자동 가입 처리")
  void loginOrRegisterOAuth_registerNewUser() {
    String email = "new@oauth.com";
    String name = "NewOAuth";
    given(userRepository.findByEmail(email)).willReturn(Optional.empty());
    given(passwordEncoder.encode(anyString())).willReturn("encoded-password");

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

    User result = userService.loginOrRegisterOAuth(email, name);

    then(userRepository).should().save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    assertThat(savedUser.getEmail()).isEqualTo(email);
    assertThat(savedUser.getName()).isEqualTo(name);
    assertThat(savedUser.getPassword()).isEqualTo("encoded-password");
    assertThat(result).isEqualTo(savedUser);
  }
}