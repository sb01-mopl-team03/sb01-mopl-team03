package team03.mopl.domain.follow.controller;

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

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.domain.user.UserResponse;

@WebMvcTest(FollowController.class)
class FollowControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private FollowService followService;

  @Test
  void testFollow() throws Exception {
    UUID followerId = UUID.randomUUID();
    UUID followingId = UUID.randomUUID();

    mockMvc.perform(post("/api/follows/{followerId}/follow/{followingId}", followerId, followingId)
            .with(csrf())
            .with(user("testuser").roles("USER")))
        .andExpect(status().isOk());

    verify(followService).follow(followerId, followingId);
  }

  @Test
  void testUnfollow() throws Exception {
    UUID followerId = UUID.randomUUID();
    UUID followingId = UUID.randomUUID();

    mockMvc.perform(delete("/api/follows/{followerId}/unfollow/{followingId}", followerId, followingId)
            .with(csrf()).with(user("testuser").roles("USER")))
        .andExpect(status().isOk());

    verify(followService).unfollow(followerId, followingId);
  }

  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testGetFollowing() throws Exception {
    UUID userId = UUID.randomUUID();
    UserResponse user = new UserResponse("user1@example.com", "user1", "USER", false);
    when(followService.getFollowing(userId)).thenReturn(List.of(user));

    mockMvc.perform(get("/api/follows/{userId}/following", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("user1@example.com"))
        .andExpect(jsonPath("$[0].name").value("user1"))
        .andExpect(jsonPath("$[0].role").value("USER"))
        .andExpect(jsonPath("$[0].isLocked").value(false));
  }

  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testGetFollowers() throws Exception {
    UUID userId = UUID.randomUUID();
    UserResponse user = new UserResponse("follower@example.com", "follower", "USER", false);
    when(followService.getFollowers(userId)).thenReturn(List.of(user));

    mockMvc.perform(get("/api/follows/{userId}/followers", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("follower@example.com"))
        .andExpect(jsonPath("$[0].name").value("follower"))
        .andExpect(jsonPath("$[0].role").value("USER"))
        .andExpect(jsonPath("$[0].isLocked").value(false));
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

