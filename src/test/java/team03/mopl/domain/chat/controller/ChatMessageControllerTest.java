package team03.mopl.domain.chat.controller;

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
import team03.mopl.domain.chat.dto.ChatMessageCreateRequest;
import team03.mopl.domain.chat.dto.ChatMessageDto;
import team03.mopl.domain.chat.dto.SystemMessageDto;
import team03.mopl.domain.chat.service.ChatMessageService;
import team03.mopl.domain.chat.service.ChatRoomService;
import team03.mopl.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {
  //웹소켓 컨트롤러 단위 테스트
  //todo
  // 웹소켓 컨트롤러는 통합테스트로 실제 STOMP 클라이언트로 테스트하는 것도 추가해야함

  @Mock
  private ChatMessageService chatMessageService;

  @Mock
  private ChatRoomService chatRoomService;

  @InjectMocks
  private ChatMessageController chatMessageController;

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

      ChatMessageCreateRequest request = new ChatMessageCreateRequest(
          mockRoomId,
          mockUserId,
          content,
          LocalDateTime.now()
      );
      //when & then
      ChatMessageDto chatMessageDto = chatMessageController.sendMessage(mockRoomId, request);

      verify(chatMessageService).create(request);
    }

    @Test
    @DisplayName("요청 파라미터로 주어진 채팅방 id와 메세지 생성 요청 바디의 채팅방Id가 일치하지 않음")
    void fail() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      UUID mockUserId = UUID.randomUUID();
      String content = "테스트 메세지입니다.";

      ChatMessageCreateRequest request = new ChatMessageCreateRequest(
          UUID.randomUUID(),
          mockUserId,
          content,
          LocalDateTime.now()
      );
      //when & then
      assertThrows(IllegalArgumentException.class,
          () -> chatMessageController.sendMessage(mockRoomId, request));

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
      SystemMessageDto systemMessageDto = chatMessageController.joinRoom(mockRoomId, mockUser);

      //then
      verify(chatRoomService).join(mockRoomId, mockUserId);

      assertEquals("ENTER", systemMessageDto.type());
      assertEquals(mockUserName + "님이 참여하셨습니다.", systemMessageDto.message());
    }
  }
}