package team03.mopl.domain.notification.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.entity.Notification;

@Repository
@NoArgsConstructor
public class EmitterRepositoryImpl implements EmitterRepository {

  /**
   * 활성 SSE 연결을 저장하는 맵
   * key: emitterId
   * value: SseEmitter
   */
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  /**
   * 알림(Notification)을 임시 저장해두는 캐시
   * key: notificationCacheId
   * value: Notification
   */
  private final Map<String, Notification> notificationCache = new ConcurrentHashMap<>();

  /**
   * emitterId를 기반으로 SseEmitter를 저장합니다.
   *
   * @param emitterId emitter의 고유 ID
   * @param sseEmitter SseEmitter 객체
   * @return 저장된 SseEmitter
   */
  @Override
  public SseEmitter saveEmitter(String emitterId, SseEmitter sseEmitter) {
    emitters.put(emitterId, sseEmitter);
    return sseEmitter;
  }

  /**
   * 클라이언트 재연결 시 재전송할 Notification을 임시 캐시에 저장합니다.
   *
   * @param notificationCacheId 알림 캐시 ID (eventId)
   * @param notification 알림 데이터
   */
  @Override
  public void saveNotificationCache(String notificationCacheId, Notification notification) {
    notificationCache.put(notificationCacheId, notification);
  }

  /**
   * 특정 userId prefix로 시작하는 모든 SseEmitter를 조회합니다.
   * (한 유저가 여러 기기로 접속한 경우 모두 반환)
   *
   * @param userId 유저 UUID
   * @return emitter Map
   */
  @Override
  public Map<String, SseEmitter> findAllEmittersByUserIdPrefix(String userId) {
    return emitters.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(userId))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * 특정 notification prefix로 시작하는 Notification 캐시를 모두 조회합니다.
   * (재연결 시 누락된 알림을 다시 전송하기 위해 사용)
   *
   * @param notificationId 알림 UUID
   * @return notification Map
   */
  @Override
  public Map<String, Notification> findAllNotificationCachesByNotificationIdPrefix(UUID notificationId) {
    return notificationCache.entrySet().stream()
        .filter(entry -> entry.getKey().startsWith(String.valueOf(notificationId)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   *
   * @param userId 유저 UUID
   * @return notification Map
   */
  @Override
  public Map<String, Notification> findAllNotificationCachesByUserIdPrefix(UUID userId) {
    return notificationCache.entrySet().stream()
        .filter(entry -> entry.getKey().contains(String.valueOf(userId)))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * 특정 emitterId로 SseEmitter를 제거합니다.
   *
   * @param id emitter ID
   */
  @Override
  public void deleteEmitterById(String id) {
    emitters.remove(id);
  }

  /**
   * 특정 notificationCacheId로 Notification 캐시를 제거합니다.
   *
   * @param notificationCacheId 캐시 ID
   */
  @Override
  public void deleteNotificationCacheById(String notificationCacheId) {
    notificationCache.remove(notificationCacheId);
  }


  /**
   * 특정 userId prefix로 시작하는 모든 Emitter를 제거합니다.
   * 유저가 로그아웃하거나 모든 연결이 끊겼을 때 사용
   *
   * @param userId 유저 UUID
   */
  @Override
  public void deleteAllEmittersByUserIdPrefix(String userId) {
    emitters.forEach((key, emitter) -> {
      if (key.startsWith(userId)) {
        emitters.remove(key);
      }
    });
  }

  /**
   *
   * 알림캐시 ID를 통해 알림캐시 삭제 
   * 한 알림 당 여러 알림캐시를 보유할 수 있음
   * @param notificationId 알림 UUID
   */
  @Override
  public void deleteNotificationCachesByNotificationIdPrefix(UUID notificationId) {
    notificationCache.forEach((key, notification) -> {
      if(key.startsWith(String.valueOf(notificationId))){
        notificationCache.remove(key);
      }
    });
  }
  /**
   *
   * 유저 아이디를 포함한 알림캐시들 삭제
   * @param userId 유저 ID
   */
  @Override
  public void deleteAllNotificationCachesByUserIdPrefix(String userId) {
    notificationCache.forEach((key, notification) -> {
      if(key.contains(userId)){
        notificationCache.remove(key);
      }
    });
  }
}