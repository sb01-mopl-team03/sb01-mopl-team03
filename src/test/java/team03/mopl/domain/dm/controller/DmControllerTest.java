package team03.mopl.domain.dm.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
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
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmPagingDto;
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

    var response = CursorPageResponseDto.<DmDto>builder()
        .data(List.of(dmDto))
        .nextCursor(null)
        .size(1)
        .totalElements(1L)
        .hasNext(false)
        .build();

    given(dmService.getDmList(eq(roomId), any(DmPagingDto.class), eq(userId)))
        .willReturn(response);

    User user = User.builder()
        .id(userId)
        .email("user@test.com")
        .name("user")
        .password("pw")
        .role(Role.USER)
        .build();

    var customUserDetails = new CustomUserDetails(user);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
    );

    // when & then
    mockMvc.perform(get("/api/dm/{roomId}", roomId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].content").value("안녕 DM"))
        .andExpect(jsonPath("$.data[0].senderId").value(userId.toString()))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.size").value(1));
  }

  @Test
  @DisplayName("DM 목록 페이징 테스트 - 1페이지 20개, 2페이지 10개")
  void getDmPagingTest() throws Exception {
    // given
    List<DmDto> firstPageData = new ArrayList<>();
    List<DmDto> secondPageData = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();

    for (int i = 0; i < 30; i++) {
      DmDto dm = new DmDto(
          UUID.randomUUID(),
          userId,
          "메시지 " + i,
          Set.of(),
          1,
          now.minusSeconds(30 - i),
          roomId
      );
      if (i < 20) firstPageData.add(dm);
      else secondPageData.add(dm);
    }

    String encodedCursor = encodeCursor(new Cursor(
        secondPageData.get(0).getCreatedAt().toString(),
        secondPageData.get(0).getId().toString()
    ));

    // 1페이지 mock 응답
    given(dmService.getDmList(eq(roomId), argThat(dto -> dto.getCursor() == null), eq(userId)))
        .willReturn(CursorPageResponseDto.<DmDto>builder()
            .data(firstPageData)
            .nextCursor(encodedCursor)
            .size(20)
            .totalElements(30L)
            .hasNext(true)
            .build());

    // 2페이지 mock 응답
    given(dmService.getDmList(eq(roomId), argThat(dto -> encodedCursor.equals(dto.getCursor())), eq(userId)))
        .willReturn(CursorPageResponseDto.<DmDto>builder()
            .data(secondPageData)
            .nextCursor(null)
            .size(10)
            .totalElements(30L)
            .hasNext(false)
            .build());

    // SecurityContext 설정
    User user = User.builder()
        .id(userId)
        .email("user@test.com")
        .name("user")
        .password("pw")
        .role(Role.USER)
        .build();
    var customUserDetails = new CustomUserDetails(user);
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities())
    );

    // when & then (1st page)
    mockMvc.perform(get("/api/dm/{roomId}", roomId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(20))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.size").value(20));

    // when & then (2nd page)
    mockMvc.perform(get("/api/dm/{roomId}?cursor={cursor}", roomId, encodedCursor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(10))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.size").value(10));
  }

  private String encodeCursor(Cursor cursor) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = objectMapper.writeValueAsString(cursor);
    return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }

}
