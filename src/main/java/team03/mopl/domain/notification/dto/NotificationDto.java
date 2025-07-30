package team03.mopl.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

@Data
@RequiredArgsConstructor
@Schema(description = "알림 응답 DTO")
public class NotificationDto {

  @Schema(description = "알림 ID", example = "f3c60d49-a1b2-43cc-9c4a-52e75fc0dd43")
  private final UUID id;

  @Schema(description = "알림 수신자 ID", example = "a7f72d4e-125f-43c0-bccd-f32a2b6c3d91")
  private final UUID receiverId;

  @Schema(description = "알림 내용", example = "새 메시지가 도착했습니다.")
  private final String content;

  @Schema(description = "알림 타입", example = "NEW_MESSAGE")
  private final NotificationType notificationType;

  @Schema(description = "생성 시각", example = "2025-07-16T10:15:30")
  private final LocalDateTime createdAt;

  @Schema(description = "읽음 확인", example = "false")
  private final Boolean isRead;

  public NotificationDto(UUID receiverId, NotificationType notificationType, String content, Boolean isRead) {
    this.id = null;
    this.receiverId = receiverId;
    this.content = content;
    this.notificationType = notificationType;
    this.createdAt = null;
    this.isRead = isRead;
  }

  public static NotificationDto from(Notification notification) {
    return new NotificationDto(
        notification.getId(),
        notification.getReceiverId(),
        notification.getContent(),
        notification.getType(),
        notification.getCreatedAt(),
        notification.isRead()
    );
  }
}
