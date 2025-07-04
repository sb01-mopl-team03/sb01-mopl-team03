package team03.mopl.domain.dm.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.service.DmRoomService;
import team03.mopl.domain.dm.service.DmService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.jwt.CustomUserDetails;

@WebMvcTest(DmRoomController.class)
@WithMockUser(roles = "USER")
class DmRoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private DmRoomService dmRoomService;

  @MockitoBean
  private DmService dmService;

  @Test
  @DisplayName("특정 DM방 조회")
  void getRoom() throws Exception {
    UUID roomId = UUID.randomUUID();
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();

    DmRoomDto dto = new DmRoomDto(roomId, userA, userB, LocalDateTime.now());
    given(dmRoomService.getRoom(roomId)).willReturn(dto);

    mockMvc.perform(get("/api/dmRooms/{dmRoomId}", roomId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(roomId.toString()))
        .andExpect(jsonPath("$.senderId").value(userA.toString()))
        .andExpect(jsonPath("$.receiverId").value(userB.toString()));
  }

  @Test
  @DisplayName("UserA, UserB 방 조회 또는 생성")
  void getOrCreateRoom() throws Exception {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    UUID roomId = UUID.randomUUID();
    User user;
    user = User.builder()
        .id(userA)
        .email("user@test.com")
        .name("user")
        .password("pw")
        .role(Role.USER)
        .build();
    CustomUserDetails principal = new CustomUserDetails(user);


    given(dmRoomService.findOrCreateRoom(userA, userB))
        .willReturn(new DmRoomDto(roomId, userA, userB, LocalDateTime.now()));

    mockMvc.perform(get("/api/dmRooms/userRoom")
            .with(user(principal))
            .param("userB", userB.toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").value(roomId.toString()));
  }

  @Test
  @DisplayName("유저의 모든 DM방 조회")
  void getAllRooms() throws Exception {
    UUID userId = UUID.randomUUID();
    UUID roomId1 = UUID.randomUUID();
    UUID roomId2 = UUID.randomUUID();

    List<DmRoomDto> rooms = List.of(
        new DmRoomDto(roomId1, userId, UUID.randomUUID(), LocalDateTime.now()),
        new DmRoomDto(roomId2, userId, UUID.randomUUID(), LocalDateTime.now())
    );

    User user;
    user = User.builder()
        .id(userId)
        .email("user@test.com")
        .name("user")
        .password("pw")
        .role(Role.USER)
        .build();

    given(dmRoomService.getAllRoomsForUser(userId)).willReturn(rooms);

    CustomUserDetails principal = new CustomUserDetails(user);

    mockMvc.perform(get("/api/dmRooms/")
            .with(user(principal)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(roomId1.toString()))
        .andExpect(jsonPath("$[1].id").value(roomId2.toString()));
  }

  @Test
  @DisplayName("방 삭제")
  void deleteRoom() throws Exception {
    UUID roomId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    User user;
    user = User.builder()
        .id(userId)
        .email("user@test.com")
        .name("user")
        .password("pw")
        .role(Role.USER)
        .build();
    CustomUserDetails principal = new CustomUserDetails(user);

    doNothing().when(dmRoomService).deleteRoom(userId, roomId);

    mockMvc.perform(delete("/api/dmRooms/{roomId}", roomId)
            .with(user(principal))
            .with(csrf()))
        .andExpect(status().isNoContent());
  }
}
