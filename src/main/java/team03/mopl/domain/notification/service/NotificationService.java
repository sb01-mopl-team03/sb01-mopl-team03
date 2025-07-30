package team03.mopl.domain.notification.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.dto.NotificationPagingDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

public interface NotificationService {
  UUID sendNotification(NotificationDto notificationDto);
  CursorPageResponseDto<NotificationDto> getNotifications(NotificationPagingDto notificationPagingDto, UUID receiverId);
  void markAllAsRead(UUID notificationId);
  void readNotification(UUID receiverId, UUID notificationId);
  void deleteNotification(UUID notificationId);
  void deleteNotificationByUserId(UUID authenticatedUserId);
}