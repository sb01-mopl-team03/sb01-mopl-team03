package team03.mopl.domain.user;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;
import team03.mopl.domain.curation.service.CurationService;
import team03.mopl.domain.playlist.service.PlaylistService;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.domain.subscription.service.SubscriptionService;


@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private ProfileImageService profileImageService;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ReviewService reviewService;

  @MockitoBean
  private SubscriptionService subscriptionService;

  @MockitoBean
  private PlaylistService playlistService;

  @MockitoBean
  private CurationService curationService;

  @WithMockUser
  @Test
  void create() throws Exception {
    UserCreateRequest request = new UserCreateRequest("홍길동", "hong@naver.com", "password",
        null);
    UserResponse response = new UserResponse("hong@naver.com", "홍길동", "user", false, null);

    when(userService.create(any(UserCreateRequest.class))).thenReturn(response);

    mockMvc.perform(post("/api/users")
            .with(csrf())
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("홍길동"))
        .andExpect(jsonPath("$.email").value("hong@naver.com"))
        .andExpect(jsonPath("$.role").value("user"))
        .andExpect(jsonPath("$.isLocked").value(false));
  }

  @WithMockUser
  @Test
  void create_with_profile() throws Exception {
    MockMultipartFile profile = new MockMultipartFile(
        "profile", "profile.png", "image/png", "dummy".getBytes());

    MockMultipartFile jsonPart = new MockMultipartFile(
        "request", "", "application/json",
        objectMapper.writeValueAsBytes(
            new UserCreateRequest("홍길동", "hong@naver.com", "password", null)
        ));

    UserResponse response = new UserResponse("hong@naver.com", "홍길동", "user", false, "profile.png");

    when(userService.create(any(UserCreateRequest.class))).thenReturn(response);

    mockMvc.perform(multipart("/api/users")
            .file(profile)
            .file(jsonPart)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("홍길동"))
        .andExpect(jsonPath("$.email").value("hong@naver.com"));
  }


  @WithMockUser
  @Test
  void find() throws Exception {
    UUID userId = UUID.randomUUID();
    UserResponse response = new UserResponse("hong@naver.com", "홍길동", "user", false, "profile.png");

    when(userService.find(userId)).thenReturn(response);

    mockMvc.perform(get("/api/users/" + userId)
            .with(csrf()))
        .andExpect(jsonPath("$.name").value("홍길동"))
        .andExpect(jsonPath("$.email").value("hong@naver.com"))
        .andExpect(jsonPath("$.role").value("user"))
        .andExpect(jsonPath("$.isLocked").value(false));
  }

  /*@WithMockUser
  @Test
  void update() throws Exception {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("강감찬", "profile2.png");
    UserResponse response = new UserResponse("hong@naver.com", "강감찬", "user", false,
        "profile2.png");

    when(userService.update(Mockito.eq(userId), any(UserUpdateRequest.class))).thenReturn(response);

    mockMvc.perform(put("/api/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("강감찬"));
  }*/

  @WithMockUser
  @Test
  void update_with_profile() throws Exception {
    UUID userId = UUID.randomUUID();

    MockMultipartFile profile = new MockMultipartFile(
        "profile", "profile2.png", "image/png", "image-data".getBytes());
    MockMultipartFile jsonPart = new MockMultipartFile(
        "request", "", "application/json",
        objectMapper.writeValueAsBytes(new UserUpdateRequest("강감찬", "newPass123")));

    UserResponse response = new UserResponse("hong@naver.com", "강감찬", "user", false, "profile2.png");

    when(userService.update(Mockito.eq(userId), any(UserUpdateRequest.class), any(MultipartFile.class)))
        .thenReturn(response);

    mockMvc.perform(multipart("/api/users/" + userId)
            .file(profile)
            .file(jsonPart)
            .with(csrf())
            .with(request -> {
              request.setMethod("PUT");
              return request;
            }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("강감찬"))
        .andExpect(jsonPath("$.profileImage").value("profile2.png"));
  }

  @WithMockUser
  @Test
  void delete() throws Exception {
    UUID userId = UUID.randomUUID();

    mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/" + userId)
            .with(csrf()))
        .andExpect(status().isNoContent());
  }

  @WithMockUser
  @Test
  void findAll() throws Exception {
    List<UserResponse> list = List.of(
        new UserResponse("hong@naver.com", "홍길동", "user", false, "profile.png")
    );

    when(userService.findAll()).thenReturn(list);

    mockMvc.perform(get("/api/users")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("홍길동"))
        .andExpect(jsonPath("$[0].name").value("홍길동"))
        .andExpect(jsonPath("$[0].email").value("hong@naver.com"))
        .andExpect(jsonPath("$[0].role").value("user"))
        .andExpect(jsonPath("$[0].isLocked").value(false));
  }

  @WithMockUser
  @Test
  void getProfileImages() throws Exception {
    List<String> profiles = List.of("profile1.png", "profile2.png");

    when(profileImageService.getProfileImages()).thenReturn(profiles);

    mockMvc.perform(get("/api/users/profiles")
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value("profile1.png"));
  }
}