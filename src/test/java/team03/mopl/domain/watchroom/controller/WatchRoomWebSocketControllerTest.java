package team03.mopl.domain.watchroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import team03.mopl.domain.watchroom.dto.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageDto;
import team03.mopl.domain.watchroom.service.WatchRoomMessageService;
import team03.mopl.domain.watchroom.service.WatchRoomService;
import team03.mopl.jwt.CustomUserDetails;

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
      UUID mockUserId = UUID.randomUUID();
      String content = "테스트 메세지입니다.";

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          mockRoomId,
          mockUserId,
          content,
          LocalDateTime.now()
      );
      //when & then
      WatchRoomMessageDto watchRoomMessageDto = watchRoomWebSocketController.sendMessage(mockRoomId, request);

      verify(watchRoomMessageService).create(request);
    }

    @Test
    @DisplayName("요청 파라미터로 주어진 채팅방 id와 메세지 생성 요청 바디의 채팅방Id가 일치하지 않음")
    void fail() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      UUID mockUserId = UUID.randomUUID();
      String content = "테스트 메세지입니다.";

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          UUID.randomUUID(),
          mockUserId,
          content,
          LocalDateTime.now()
      );
      //when & then
      assertThrows(IllegalArgumentException.class,
          () -> watchRoomWebSocketController.sendMessage(mockRoomId, request));

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
      UUID mockUserId = UUID.randomUUID();
      String mockUserName = "테스트 사용자 이름";

      CustomUserDetails mockUser = mock(CustomUserDetails.class);
      when(mockUser.getId()).thenReturn(mockUserId);
      when(mockUser.getUsername()).thenReturn(mockUserName);

      //when
      watchRoomWebSocketController.joinRoom(mockRoomId, mockUser);

      //then
      verify(watchRoomService).joinChatRoomAndGetInfo(mockRoomId, mockUserId);
      verify(watchRoomService).getParticipants(mockRoomId);

    }
  }
}