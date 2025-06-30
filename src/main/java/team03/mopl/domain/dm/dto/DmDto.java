package team03.mopl.domain.dm.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import team03.mopl.domain.dm.entity.Dm;

@Getter
public class DmDto {
  private final UUID id;
  private final UUID senderId;
  private final String content;
  private final List<UUID> readUserIds;  // ← 대문자 U
  private final int unreadCount;
  private final LocalDateTime createdAt;
  private final UUID roomId;

  @Builder
  public DmDto(UUID id, UUID senderId, String content, List<UUID> readUserIds,
      int unreadCount, LocalDateTime createdAt, UUID roomId) {
    this.id = id;
    this.senderId = senderId;
    this.content = content;
    this.readUserIds = readUserIds;
    this.unreadCount = unreadCount;
    this.createdAt = createdAt;
    this.roomId = roomId;
  }

  public static DmDto from(Dm dm) {
    return DmDto.builder()
        .id(dm.getId())
        .senderId(dm.getSenderId())
        .content(dm.getContent())
        .readUserIds(dm.getReadUserIds())
        .unreadCount(dm.getUnreadCount())
        .createdAt(dm.getCreatedAt())
        .roomId(dm.getDmRoom().getId())
        .build();
  }
}

