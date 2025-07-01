package team03.mopl.domain.notification.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService{
  private final NotificationRepository notificationRepository;
  private final SseEmitterManager sseEmitterManager;

  @Override
  public void sendNotification(UUID receiverId, NotificationType type, String content) {
    Notification notification = new Notification(receiverId, type, content);
    Notification save = notificationRepository.save(notification);
    sseEmitterManager.sendNotification(receiverId, save);
  }

  @Override
  public List<Notification> getNotifications(UUID receiverId) {
    return notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
  }

  @Override
  public void markAllAsRead(UUID receiverId) {
    List<Notification> unread = notificationRepository.findByReceiverIdAndRead(receiverId, false);
    unread.forEach(Notification::setIsRead);
    notificationRepository.saveAll(unread);
  }

}
