package team03.mopl.domain.notification.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

public interface NotificationService {
  void sendNotification(UUID receiverId, NotificationType type, String content);
  List<NotificationDto> getNotifications(UUID receiverId);
  void markAllAsRead(UUID notificationId);
}

