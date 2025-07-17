package team03.mopl.domain.notification.repository;

import java.util.Map;
import java.util.UUID;
import team03.mopl.domain.notification.entity.Notification;

public interface EmitterCacheRepository {

  /**
   * 클라이언트 재연결 시 전송할 알림(Notification)을 임시로 캐싱합니다.
   *
   * @param notificationCacheId 캐시 알림 ID)
   * @param notification 알림 데이터
   */
  void saveNotificationCache(String notificationCacheId, Notification notification);


  /**
   * 특정 유저 ID(prefix)로 시작하는 Notification 캐시를 모두 조회합니다.
   * 재연결 시, 해당 유저의 수신하지 못한 알림을 재전송하기 위해 사용합니다.
   *
   * @param notificationId 알림의 UUID
   * @return Notification Map
   */
  Map<String, Notification> findAllNotificationCachesByNotificationIdPrefix(UUID notificationId);

  /**
   *
   *
   * @param userId 유저의 UUID
   * @return Notification Map
   */
  Map<String, Notification> findAllNotificationCachesByUserIdPrefix(UUID userId);

  void deleteNotificationCacheById(String notificationCacheId);

  void deleteNotificationCachesByNotificationIdPrefix(UUID notificationId);
  void deleteAllNotificationCachesByUserIdPrefix(String userId);
}
