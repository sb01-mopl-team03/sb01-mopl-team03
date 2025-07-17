package team03.mopl.domain.notification.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.entity.Notification;

@Repository
@NoArgsConstructor
@Slf4j
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
   * 특정 emitterId로 SseEmitter를 제거합니다.
   *
   * @param id emitter ID
   */
  @Override
  public void deleteEmitterById(String id) {
    emitters.remove(id);
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

}