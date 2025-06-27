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
import team03.mopl.common.exception.user.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserServiceImpl userService;

  private UserCreateRequest userCreateRequest;

  @BeforeEach
  void setUp() {
    userCreateRequest = new UserCreateRequest("test@email.com", "test", "test");
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
  @DisplayName("유저 비밀번호 수정")
  void updateUser() {
    // given
    UUID id = UUID.randomUUID();
    User user = User.builder()
        .id(id)
        .email("aaa@a.com")
        .name("수정맨")
        .password("old")
        .role(Role.USER)
        .build();

    UserUpdateRequest request = new UserUpdateRequest("ㅎㅎ","newpass");
    given(userRepository.findById(id)).willReturn(Optional.of(user));
    given(passwordEncoder.encode("newpass")).willReturn("encoded-newpass");

    // when
    UserResponse response = userService.update(id, request);

    // then
    assertThat(user.getPassword()).isEqualTo("encoded-newpass");
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
}