package team03.mopl.domain.follow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.domain.follow.dto.FollowResponse;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.jwt.CustomUserDetails;

@WebMvcTest(FollowController.class)
class FollowControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private FollowService followService;

  @TestConfiguration
  static class configuration {
    @Bean
    public FollowService followService() {
      return mock(FollowService.class);
    }
  }

  @Test
  @WithMockUser(roles = "USER")
  void testFollow() throws Exception {
    UUID followerId = UUID.randomUUID();
    UUID followingId = UUID.randomUUID();
    User follower = User.builder()
        .id(followerId)
        .email("test@example.com")
        .name("tester")
        .password("encoded_password")
        .role(Role.USER) // 또는 Role.ADMIN
        .build();
    CustomUserDetails principal = new CustomUserDetails(follower);

    mockMvc.perform(post("/api/follows/{followingId}", followingId)
            .with(csrf())
            .with(user(principal))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());


    verify(followService).follow(followerId, followingId);
  }
  
  @Test
  @WithMockUser(roles = "USER")
  @DisplayName("팔로우하는 사람과 팔로우 대상이 같다")
  void follow_BadRequestFollowingException() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID(); // 로그인된 사용자 ID
    String followingId = userId.toString(); // 자기 자신 팔로우 시도

    User user = User.builder()
        .id(userId)
        .email("test@example.com")
        .name("self-follower")
        .password("encoded_pw")
        .role(Role.USER)
        .build();

    CustomUserDetails principal = new CustomUserDetails(user);

    // Act & Assert
    mockMvc.perform(post("/api/follows/{followingId}", followingId)
            .with(csrf())
            .with(user(principal))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST_FOLLOWING"))
        .andExpect(jsonPath("$.message").value("로그인된 사람과 팔로우하는 사람이 다릅니다"));

    // followService는 호출되면 안됨
    verify(followService, never()).follow(any(), any());
  }


  @Test
  @WithMockUser(roles = "USER")
  void testUnfollow() throws Exception {
    UUID followerId = UUID.randomUUID();
    UUID followingId = UUID.randomUUID();
    User follower = User.builder()
        .id(followerId)
        .email("test@example.com")
        .name("tester")
        .password("encoded_password")
        .role(Role.USER) // 또는 Role.ADMIN
        .build();
    CustomUserDetails principal = new CustomUserDetails(follower);

    mockMvc.perform(delete("/api/follows/{followingId}", followingId)
            .with(csrf())
            .with(user(principal))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    verify(followService).unfollow(followerId, followingId);
  }

  @Test
  @WithMockUser(roles = "USER")
  @DisplayName("언팔로우하는 사람과 언팔로우 대상이 같다")
  void unfollow_BadRequestFollowingException() throws Exception {
    // Arrange
    UUID userId = UUID.randomUUID(); // 로그인된 사용자 ID
    String followingId = userId.toString(); // 자기 자신 팔로우 시도

    User user = User.builder()
        .id(userId)
        .email("test@example.com")
        .name("self-follower")
        .password("encoded_pw")
        .role(Role.USER)
        .build();

    CustomUserDetails principal = new CustomUserDetails(user);

    // Act & Assert
    mockMvc.perform(delete("/api/follows/{followingId}", followingId)
            .with(csrf())
            .with(user(principal))
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST_FOLLOWING"))
        .andExpect(jsonPath("$.message").value("로그인된 사람과 팔로우하는 사람이 다릅니다"));

    // followService는 호출되면 안됨
    verify(followService, never()).follow(any(), any());
  }

  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testGetFollowing() throws Exception {
    UUID userId = UUID.randomUUID();
    FollowResponse user = new FollowResponse( userId, "user1@example.com", "user1", "USER", null);
    when(followService.getFollowing(userId)).thenReturn(List.of(user));

    mockMvc.perform(get("/api/follows/{userId}/following", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("user1@example.com"))
        .andExpect(jsonPath("$[0].name").value("user1"))
        .andExpect(jsonPath("$[0].role").value("USER"));
  }

  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testGetFollowers() throws Exception {
    UUID userId = UUID.randomUUID();
    FollowResponse user = new FollowResponse( userId,"follower@example.com", "follower", "USER", null);
    when(followService.getFollowers(userId)).thenReturn(List.of(user));

    mockMvc.perform(get("/api/follows/{userId}/followers", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("follower@example.com"))
        .andExpect(jsonPath("$[0].name").value("follower"))
        .andExpect(jsonPath("$[0].role").value("USER"));
  }

  @Test
  @WithMockUser(roles = "USER")
  void testIsFollowing() throws Exception {
    UUID followerId = UUID.randomUUID();
    UUID followingId = UUID.randomUUID();

    when(followService.isFollowing(followerId, followingId)).thenReturn(true);

    mockMvc.perform(get("/api/follows/{followerId}/is-following/{followingId}", followerId, followingId))
        .andExpect(status().isOk())
        .andExpect(content().string("true"));
  }
}

