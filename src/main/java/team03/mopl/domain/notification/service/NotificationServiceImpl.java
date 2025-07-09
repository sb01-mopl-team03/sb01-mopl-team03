package team03.mopl.domain.notification.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService{
  private final NotificationRepository notificationRepository;
  private final EmitterService emitterService;

  @Override
  public UUID sendNotification(NotificationDto notificationDto) {
    log.info("sendNotification - 알림 전송 요청: receiverId={}, type={}, content={}",
        notificationDto.getReceiverId(),
        notificationDto.getNotificationType(),
        notificationDto.getContent());
    Notification notification = new Notification(notificationDto.getReceiverId(), notificationDto.getNotificationType(), notificationDto.getContent());
    Notification saved = notificationRepository.save(notification);
    emitterService.sendNotificationToMember(notificationDto.getReceiverId(), saved );
    //sseEmitterManager.sendNotification(receiverId, save);
    log.info("알림 전송 완료: notificationId={}, receiverId={}",
        saved.getId(), saved.getReceiverId());
    return notification.getId();
  }

  @Override
  public List<NotificationDto> getNotifications(UUID receiverId) {
    log.info("getNotifications - 알림 내역 조회: receiverId={}", receiverId);
    List<NotificationDto> notifications = notificationRepository
        .findByReceiverIdOrderByCreatedAtDesc(receiverId)
        .stream()
        .map(NotificationDto::from)
        .toList();
    log.info("getNotifications - 알림 내역 조회 완료: receiverId={}, 알림 수={}", receiverId, notifications.size());
    return notifications;
  }

  @Override
  public void markAllAsRead(UUID receiverId) {
    log.info("markAllAsRead - 알림 읽음 처리 시작: receiverId={}", receiverId);

    List<Notification> unread = notificationRepository
        .findByReceiverIdAndIsRead(receiverId, false);

    unread.forEach(Notification::setIsRead);
    notificationRepository.saveAll(unread);
    //읽었다고 판단한 알림들은 재전송용 캐시에서 삭제
    emitterService.deleteNotificationCaches(unread);
    log.info("markAllAsRead - 알림 읽음 처리 완료: receiverId={}, 읽은 알림 수={}", receiverId, unread.size());
  }

}
