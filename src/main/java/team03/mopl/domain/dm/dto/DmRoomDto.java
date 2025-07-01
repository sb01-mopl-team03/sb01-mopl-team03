package team03.mopl.domain.dm.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team03.mopl.domain.dm.entity.DmRoom;

@Getter
@NoArgsConstructor
public class DmRoomDto {

  private UUID id;
  private UUID senderId;
  private UUID receiverId;
  private LocalDateTime createdAt;
  private String lastMessage;
  private long newMessageCount;

  public DmRoomDto(UUID id, UUID senderId, UUID receiverId, LocalDateTime createdAt) {
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.createdAt = createdAt;
  }

  public DmRoomDto(UUID id, UUID senderId, UUID receiverId, LocalDateTime createdAt, String lastMessage, long newMessageCount) {
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.createdAt = createdAt;
    this.lastMessage = lastMessage;
    this.newMessageCount = newMessageCount;
  }

  public static DmRoomDto from(String lastMessage, int unreadCount, DmRoom room) {
    return new DmRoomDto(
        room.getId(),
        room.getSenderId(),
        room.getReceiverId(),
        room.getCreatedAt(),
        lastMessage,
        unreadCount
    );
  }


  public static DmRoomDto from(DmRoom room) {
    return new DmRoomDto(
        room.getId(),
        room.getSenderId(),
        room.getReceiverId(),
        room.getCreatedAt()
    );
  }
}
