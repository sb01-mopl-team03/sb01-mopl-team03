package team03.mopl.domain.watchroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import team03.mopl.domain.watchroom.dto.watchroommessage.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.watchroommessage.WatchRoomMessageDto;
import team03.mopl.domain.watchroom.service.WatchRoomMessageService;
import team03.mopl.domain.watchroom.service.WatchRoomService;

@ExtendWith(MockitoExtension.class)
class WatchRoomWebSocketControllerTest {

  @Mock
  private WatchRoomMessageService watchRoomMessageService;

  @Mock
  private WatchRoomService watchRoomService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private WatchRoomWebSocketController watchRoomWebSocketController;

  @Nested
  @DisplayName("채팅방 메세지 전송 요청")
  class sendMessage {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      String username = "테스트유저이름";
      String content = "테스트 메세지입니다.";

      Principal principal = new Principal() {
        @Override
        public String getName() {
          return username;
        }
      };

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          mockRoomId,
          content,
          LocalDateTime.now()
      );
      //when & then
      WatchRoomMessageDto watchRoomMessageDto = watchRoomWebSocketController.sendMessage(mockRoomId,
          request, principal);

      verify(watchRoomMessageService).create(request, username);
    }

    @Test
    @DisplayName("요청 파라미터로 주어진 채팅방 id와 메세지 생성 요청 바디의 채팅방Id가 일치하지 않음")
    void fail() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      String content = "테스트 메세지입니다.";

      Principal principal = mock(Principal.class);

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          UUID.randomUUID(),
          content,
          LocalDateTime.now()
      );
      //when & then
      assertThrows(IllegalArgumentException.class,
          () -> watchRoomWebSocketController.sendMessage(mockRoomId, request, principal));

    }

  }

  @Nested
  @DisplayName("채팅방 입장 요청")
  class join {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      String mockUserName = "test@test.com";

      Principal principal = new Principal() {
        @Override
        public String getName() {
          return mockUserName;
        }
      };

      //when
      watchRoomWebSocketController.joinRoom(mockRoomId, principal);

      //then
      verify(watchRoomService).joinWatchRoomAndGetInfo(mockRoomId, principal.getName());
      verify(watchRoomService).getParticipants(mockRoomId);

    }
  }
}