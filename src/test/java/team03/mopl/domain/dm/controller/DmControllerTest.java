package team03.mopl.domain.dm.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.service.DmService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.jwt.CustomUserDetails;

@WebMvcTest(DmController.class)
@WithMockUser(roles = "USER")
class DmControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private DmService dmService;

  private UUID roomId;
  private UUID userId;

  @TestConfiguration
  static class MockConfig {
    @Bean
    public DmService dmService() {
      return mock(DmService.class);
    }
  }

  @BeforeEach
  void setUp() {
    roomId = UUID.randomUUID();
    userId = UUID.randomUUID();
  }

  @Test
  @DisplayName("DM 목록 조회 API")
  void getDmList() throws Exception {
    // given
    Set<UUID> readUsers = Set.of(userId);
    var dmDto = new DmDto(
        UUID.randomUUID(),
        userId,
        "안녕 DM",
        readUsers,
        2 - readUsers.size(),
        LocalDateTime.now(),
        roomId
    );

    given(dmService.getDmList(roomId, userId)).willReturn(List.of(dmDto));

    User user = User.builder()
        .id(userId)
        .email("user@test.com")
        .name("user")
        .password("pw")
        .role(Role.USER)
        .build();

    // 커스텀 유저 디테일을 SecurityContext에 직접 주입
    var customUserDetails = new CustomUserDetails(user);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
    );

    // when & then
    mockMvc.perform(get("/api/dm/{roomId}", roomId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].content").value("안녕 DM"))
        .andExpect(jsonPath("$[0].senderId").value(userId.toString()));
  }

}
