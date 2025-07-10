package team03.mopl.domain.watchroom.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;
import team03.mopl.domain.watchroom.exception.WatchRoomRoomNotFoundException;
import team03.mopl.domain.watchroom.service.WatchRoomService;


@WebMvcTest(controllers = WatchRoomController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class})
@DisplayName("채팅방 기능 컨트롤러 단위 테스트")
class WatchRoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private WatchRoomService watchRoomService;

  @Nested
  @DisplayName("채팅방 생성 요청")
  class createWatchRoom {

    @Test
    @DisplayName("성공")
    @WithMockUser
    void success() throws Exception {
      //given
      UUID roomId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      WatchRoomCreateRequest request = new WatchRoomCreateRequest(ownerId, contentId);
      String requestBody = objectMapper.writeValueAsString(request);

      WatchRoomDto responseDto = new WatchRoomDto(roomId, "테스트용 콘텐츠 제목", ownerId, "유저1",
          LocalDateTime.now(), 1L);

      when(watchRoomService.create(request)).thenReturn(responseDto);

      //when & then
      mockMvc.perform(post("/api/rooms")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(roomId.toString()))
          .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
          .andExpect(jsonPath("$.contentTitle").value("테스트용 콘텐츠 제목"));
    }

    @Test
    @DisplayName("실패")
    void fails() {
    }
  }

  @Nested
  @DisplayName("채팅방 전체 조회 요청")
  class getWatchRooms {

    @Test
    @DisplayName("성공")
    void success() throws Exception {
      UUID roomId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();

      WatchRoomDto responseDto1 = new WatchRoomDto(roomId, "인터스텔라", ownerId, "유저1",
          LocalDateTime.now(), 1L);
      WatchRoomDto responseDto2 = new WatchRoomDto(roomId, "장고", ownerId, "유저1",
          LocalDateTime.now(), 2L);
      WatchRoomDto responseDto3 = new WatchRoomDto(roomId, "오징어게임5", ownerId, "유저1",
          LocalDateTime.now(), 3L);

      List<WatchRoomDto> responseDtos = List.of(responseDto1, responseDto2, responseDto3);

      when(watchRoomService.getAll()).thenReturn(responseDtos);

      //when & then
      mockMvc.perform(get("/api/rooms")
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$", hasSize(3)));
    }
  }

  @Nested
  @DisplayName("채팅방 단일 조회")
  class getWatchRoom {

    @Test
    @DisplayName("성공")
    void success() throws Exception {
      UUID roomId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();

      WatchRoomDto responseDto = new WatchRoomDto(roomId, "테스트 콘텐츠 제목", ownerId, "유저1",
          LocalDateTime.now(), 1L);

      when(watchRoomService.getById(roomId)).thenReturn(responseDto);

      //when & then
      mockMvc.perform(get("/api/rooms/" + roomId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").exists())  // 모든 요소가 id를 가지는지
          .andExpect(jsonPath("$.id").value(roomId.toString()))
          .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
          .andExpect(jsonPath("$.contentTitle").value("테스트 콘텐츠 제목"));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 ID")
    void fails() throws Exception {
      UUID randomId = UUID.randomUUID();

      when(watchRoomService.getById(randomId)).thenThrow(new WatchRoomRoomNotFoundException());

      //when & then
      mockMvc.perform(get("/api/rooms/" + randomId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }
}