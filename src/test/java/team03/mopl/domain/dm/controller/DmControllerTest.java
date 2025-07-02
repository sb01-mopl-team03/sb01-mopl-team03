package team03.mopl.domain.dm.controller;

import static org.mockito.BDDMockito.given;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;


@WebMvcTest(DmController.class)
@WithMockUser(roles = "USER")
class DmControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private DmService dmService;

  @MockBean
  private DmRoomService dmRoomService;

  private UUID roomId;
  private UUID userId;

  @BeforeEach
  void setUp() {
    roomId = UUID.randomUUID();
    userId = UUID.randomUUID();
  }

  @Test
  @DisplayName("DM 목록 조회 API")
  void getDmList() throws Exception {
    // given
    Set<UUID> set = Set.of(userId);
    var dmDto = new DmDto(UUID.randomUUID(), userId, "안녕 DM", set, 2-set.size(), LocalDateTime.now(), roomId);
    given(dmService.getDmList(roomId, userId)).willReturn(List.of(dmDto));

    // when & then
    mockMvc.perform(get("/api/dm/{roomId}/dm", roomId)
            .param("userId", userId.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].content").value("안녕 DM"))
        .andExpect(jsonPath("$[0].senderId").value(userId.toString()));

  }
}
