package team03.mopl.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.chat.entity.ChatMessage;

public record ChatMessageDto(

    UUID id,
    UUID senderId,
    UUID chatRoomId,
    String content,
    LocalDateTime createdAt
) {

  public static ChatMessageDto from(ChatMessage chatMessage) {
    return new ChatMessageDto(
        chatMessage.getId(),
        chatMessage.getSender().getId(),
        chatMessage.getChatRoom().getId(),
        chatMessage.getContent(),
        chatMessage.getCreatedAt()
    );
  }

}
