package team03.mopl.domain.chat.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.chat.dto.ChatRoomCreateRequest;
import team03.mopl.domain.chat.dto.ChatRoomDto;
import team03.mopl.domain.chat.exception.ChatRoomNotFoundException;
import team03.mopl.domain.chat.service.ChatRoomService;

@WebMvcTest(controllers = ChatRoomController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        OAuth2ClientAutoConfiguration.class,
        OAuth2ClientWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class})
@DisplayName("채팅방 기능 컨트롤러 단위 테스트")
class ChatRoomControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ChatRoomService chatRoomService;

  @Nested
  @DisplayName("채팅방 생성 요청")
  class createChatRoom {

    @Test
    @DisplayName("성공")
    @WithMockUser
    void success() throws Exception {
      //given
      UUID roomId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      ChatRoomCreateRequest request = new ChatRoomCreateRequest(ownerId, contentId);
      String requestBody = objectMapper.writeValueAsString(request);

      ChatRoomDto responseDto = new ChatRoomDto(roomId, contentId, ownerId, 1L);

      when(chatRoomService.create(request)).thenReturn(responseDto);

      //when & then
      mockMvc.perform(post("/api/rooms")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(roomId.toString()))
          .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
          .andExpect(jsonPath("$.contentId").value(contentId.toString()));
    }

    @Test
    @DisplayName("실패")
    void fails() {
    }
  }

  @Nested
  @DisplayName("채팅방 전체 조회 요청")
  class getChatRooms {

    @Test
    @DisplayName("성공")
    void success() throws Exception {
      UUID roomId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      ChatRoomDto responseDto1 = new ChatRoomDto(roomId, contentId, ownerId, 1L);
      ChatRoomDto responseDto2 = new ChatRoomDto(roomId, contentId, ownerId, 2L);
      ChatRoomDto responseDto3 = new ChatRoomDto(roomId, contentId, ownerId, 3L);

      List<ChatRoomDto> responseDtos = List.of(responseDto1, responseDto2, responseDto3);

      when(chatRoomService.getAll()).thenReturn(responseDtos);

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
  class getChatRoom {

    @Test
    @DisplayName("성공")
    void success() throws Exception {
      UUID roomId = UUID.randomUUID();
      UUID ownerId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      ChatRoomDto responseDto = new ChatRoomDto(roomId, contentId, ownerId, 1L);

      when(chatRoomService.getById(roomId)).thenReturn(responseDto);

      //when & then
      mockMvc.perform(get("/api/rooms/"+ roomId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").exists())  // 모든 요소가 id를 가지는지
          .andExpect(jsonPath("$.id").value(roomId.toString()))
          .andExpect(jsonPath("$.ownerId").value(ownerId.toString()))
          .andExpect(jsonPath("$.contentId").value(contentId.toString()));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 ID")
    void fails() throws Exception {
      UUID randomId = UUID.randomUUID();

      when(chatRoomService.getById(randomId)).thenThrow(new ChatRoomNotFoundException());

      //when & then
      mockMvc.perform(get("/api/rooms/"+ randomId)
              .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isNotFound());
    }
  }
}