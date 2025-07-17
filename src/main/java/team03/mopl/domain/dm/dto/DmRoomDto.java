package team03.mopl.domain.dm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team03.mopl.domain.dm.entity.DmRoom;

@Getter
@NoArgsConstructor
@Schema(description = "DM 채팅방 정보 DTO")
public class DmRoomDto {

  @Schema(description = "채팅방 ID", example = "6a7b8c9d-0e1f-1234-5678-9abcdef01234")
  private UUID id;

  @Schema(description = "보낸 사람 ID", example = "11111111-2222-3333-4444-555555555555")
  private UUID senderId;

  @Schema(description = "받는 사람 ID", example = "66666666-7777-8888-9999-000000000000")
  private UUID receiverId;

  @Schema(description = "보낸 사람 이름", example = "홍길동")
  private String senderName;

  @Schema(description = "받는 사람 이름", example = "임꺽정")
  private String receiverName;

  @Schema(description = "채팅방 생성 시간", example = "2024-12-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "마지막 메시지 내용", example = "지금 확인할게요")
  private String lastMessage;

  @Schema(description = "읽지 않은 메시지 수", example = "3")
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
