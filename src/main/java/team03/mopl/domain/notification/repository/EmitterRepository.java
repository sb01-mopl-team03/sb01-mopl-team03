package team03.mopl.domain.notification.repository;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.entity.Notification;

public interface EmitterRepository {

  /**
   * 새로운 SSE Emitter를 저장합니다.
   *
   * @param emitterId Emitter의 고유 ID
   * @param sseEmitter SseEmitter 객체
   * @return 저장된 SseEmitter
   */
  SseEmitter saveEmitter(String emitterId, SseEmitter sseEmitter);

  /**
   * 클라이언트 재연결 시 전송할 알림(Notification)을 임시로 캐싱합니다.
   *
   * @param notificationCacheId 캐시 알림 ID)
   * @param notification 알림 데이터
   */
  void saveNotificationCache(String notificationCacheId, Notification notification);

  /**
   * 특정 유저 ID(prefix)로 시작하는 Emitter들을 모두 조회합니다.
   * 한 유저가 여러 기기로 접속했을 때, 그 모든 Emitter에 알림을 보내기 위해 사용합니다.
   *
   * @param userId 유저의 UUID
   * @return Emitter Map
   */
  Map<String, SseEmitter> findAllEmittersByUserIdPrefix(String userId);

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

  void deleteEmitterById(String emitterId);
  void deleteNotificationCacheById(String notificationCacheId);

  void deleteAllEmittersByUserIdPrefix(String userId);
  void deleteNotificationCachesByNotificationIdPrefix(UUID notificationId);
  void deleteAllNotificationCachesByUserIdPrefix(String userId);
}


