package team03.mopl.domain.watchroom.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.watchroom.entity.WatchRoomMessage;

public record WatchRoomMessageDto(

    UUID id,
    UUID senderId,
    String senderName,
    UUID chatRoomId,
    String content,
    LocalDateTime createdAt
) {

  public static WatchRoomMessageDto from(WatchRoomMessage watchRoomMessage) {
    return new WatchRoomMessageDto(
        watchRoomMessage.getId(),
        watchRoomMessage.getSender().getId(),
        watchRoomMessage.getSender().getName(),
        watchRoomMessage.getWatchRoom().getId(),
        watchRoomMessage.getContent(),
        watchRoomMessage.getCreatedAt()
    );
  }

}
