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
  private String senderName;
  private String receiverName;
  private LocalDateTime createdAt;
  private String lastMessage;
  private long newMessageCount;

  public DmRoomDto(UUID id, UUID senderId, UUID receiverId,  String senderName, String receiverName, LocalDateTime createdAt) {
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.senderName = senderName;
    this.receiverName = receiverName;
    this.createdAt = createdAt;
  }

  public DmRoomDto(UUID id, UUID senderId, UUID receiverId, String senderName, String receiverName, LocalDateTime createdAt, String lastMessage,
      long newMessageCount) {
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.senderName = senderName;
    this.receiverName = receiverName;
    this.createdAt = createdAt;
    this.lastMessage = lastMessage;
    this.newMessageCount = newMessageCount;
  }

  public static DmRoomDto from(String lastMessage, int unreadCount,String senderName, String receiverName, DmRoom room) {
    return new DmRoomDto(
        room.getId(),
        room.getSenderId(),
        room.getReceiverId(),
        senderName,
        receiverName,
        room.getCreatedAt(),
        lastMessage,
        unreadCount
    );
  }


  public static DmRoomDto from(String senderName, String receiverName, DmRoom room) {
    return new DmRoomDto(
        room.getId(),
        room.getSenderId(),
        room.getReceiverId(),
        senderName,
        receiverName,
        room.getCreatedAt()
    );
  }
}
