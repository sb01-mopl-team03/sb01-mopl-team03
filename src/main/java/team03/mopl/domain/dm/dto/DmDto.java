package team03.mopl.domain.dm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import team03.mopl.domain.dm.entity.Dm;

@Getter
@Schema(description = "DM 메시지 응답 DTO")
public class DmDto {

  @Schema(description = "DM ID", example = "1e2d3c4b-5a6f-7d8e-9a01-2b3c4d5e6f70")
  private final UUID id;

  @Schema(description = "보낸 사람 ID", example = "123e4567-e89b-12d3-a456-426614174000")
  private final UUID senderId;

  @Schema(description = "메시지 내용", example = "안녕하세요!")
  private final String content;

  @Schema(description = "읽은 사용자 ID 목록")
  private final Set<UUID> readUserIds;

  @Schema(description = "읽지 않은 사용자 수", example = "2")
  private final int unreadCount;

  @Schema(description = "생성 시간", example = "2024-12-01T15:30:00")
  private final LocalDateTime createdAt;

  @Schema(description = "채팅방 ID", example = "b3c4d5e6-f701-2345-6789-abcdef012345")
  private final UUID roomId;

  @Builder
  public DmDto(UUID id, UUID senderId, String content, Set<UUID> readUserIds,
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

