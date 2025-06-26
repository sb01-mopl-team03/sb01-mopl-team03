package team03.mopl.domain.dm.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DmRoomDto {

  private UUID id;
  private UUID senderId;
  private UUID receiverId;
  private LocalDateTime createdAt;

  public DmRoomDto(UUID id, UUID senderId, UUID receiverId, LocalDateTime createdAt) {
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.createdAt = createdAt;
  }

  public static DmRoomDto from(team03.mopl.domain.dm.entity.DmRoom room) {
    return new DmRoomDto(
        room.getId(),
        room.getSenderId(),
        room.getReceiverId(),
        room.getCreatedAt()
    );
  }
}
