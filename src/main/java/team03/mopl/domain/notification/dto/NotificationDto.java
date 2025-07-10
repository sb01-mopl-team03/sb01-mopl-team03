package team03.mopl.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

@Data
@RequiredArgsConstructor
public class NotificationDto {

  private final UUID receiverId;
  private final String content;
  private final NotificationType notificationType;
  private final LocalDateTime createdAt;

  public NotificationDto(String content, NotificationType notificationType, LocalDateTime createdAt) {
    this.receiverId = null;
    this.content = content;
    this.notificationType = notificationType;
    this.createdAt = createdAt;
  }
  public NotificationDto(UUID receiverId, NotificationType notificationType,String content) {
    this.receiverId = receiverId;
    this.content = content;
    this.notificationType = notificationType;
    this.createdAt = null;
  }

  public static NotificationDto from(String content, NotificationType notificationType, LocalDateTime createdAt) {
    return new NotificationDto(content, notificationType, createdAt);
  }
  public static NotificationDto from(Notification notification) {
    return new NotificationDto(notification.getReceiverId(), notification.getContent(), notification.getType(), notification.getCreatedAt());
  }
}
