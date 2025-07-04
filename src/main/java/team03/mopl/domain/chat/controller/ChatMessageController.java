package team03.mopl.domain.chat.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import team03.mopl.domain.chat.dto.ChatMessageCreateRequest;
import team03.mopl.domain.chat.dto.ChatMessageDto;
import team03.mopl.domain.chat.dto.SystemMessageDto;
import team03.mopl.domain.chat.service.ChatMessageService;
import team03.mopl.domain.chat.service.ChatRoomService;
import team03.mopl.jwt.CustomUserDetails;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {

  private final ChatRoomService chatRoomService;
  private final ChatMessageService chatMessageService;

  @MessageMapping("/rooms/{roomId}/send")
  @SendTo("/topic/rooms/{roomId}")
  public ChatMessageDto sendMessage(@DestinationVariable UUID roomId,
      ChatMessageCreateRequest request) {
    if (!request.chatRoomId().equals(roomId)) {
      //본문의 ChatRoom id와 url의 id가 같은지 한번 더 검증함
      throw new IllegalArgumentException("Room ID가 일치하지 않습니다.");
    }
    return chatMessageService.create(request);
  }

  @MessageMapping("/chat/room/{roomId}/join")
  @SendTo("/topic/room/{roomId}")
  public SystemMessageDto joinRoom(@DestinationVariable UUID roomId, @AuthenticationPrincipal
  CustomUserDetails userDetails) {

    chatRoomService.join(roomId, userDetails.getId());

    return new SystemMessageDto("ENTER", userDetails.getUsername() + "님이 참여하셨습니다.");
  }
}
