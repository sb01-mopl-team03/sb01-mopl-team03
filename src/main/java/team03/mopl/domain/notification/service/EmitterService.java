package team03.mopl.domain.notification.service;

import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.repository.EmitterRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmitterService {

  private final EmitterRepository emitterRepository;
  private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(5);

  public SseEmitter subscribe(UUID userId, String lastNotificationId) {
    // 기존 연결 정리 먼저 수행
    cleanupExistingConnections(userId);

    // 한 계정이 여러 기기로 접속할 수 있기에 같은 유저라도 다른 ID 값 필요
    String emitterId = makeEmitterId(userId);
    log.info("subscribe - SSE 구독 시작: userId={}", userId);

    // 타임아웃을 10분으로 설정 (heartbeat로 연결 유지)
    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(10));
    emitterRepository.saveEmitter(emitterId, emitter);

    // 연결 종료, 타임아웃, 에러 시 콜백 등록
    emitter.onCompletion(() -> handleEmitterTermination(emitterId, "onCompletion"));
    emitter.onTimeout(() -> handleEmitterTermination(emitterId, "onTimeout"));
    emitter.onError(e -> handleEmitterError(emitterId, e));

    // Heartbeat 스케줄링 (30초마다)
    scheduleHeartbeat(emitterId, emitter, userId);

    // 미수신 데이터 재전송
    if (hasLostData(lastNotificationId)) {
      log.info("SSE 재연결 - 누락된 데이터 복원 시도: lastNotificationId={}", lastNotificationId);
      sendLostData(lastNotificationId, userId, emitter);
    }

    return emitter;
  }

  /**
   * 기존 연결 정리 - 동일 사용자의 이전 연결들을 안전하게 종료
   */
  private void cleanupExistingConnections(UUID userId) {
    var existingEmitters = emitterRepository.findAllEmittersByUserIdPrefix(userId.toString());
    int cleanedCount = 0;

    for (var entry : existingEmitters.entrySet()) {
      String emitterId = entry.getKey();
      SseEmitter emitter = entry.getValue();

      try {
        emitter.complete();
        emitterRepository.deleteEmitterById(emitterId);
        cleanedCount++;
      } catch (Exception e) {
        log.warn("기존 연결 정리 중 오류: emitterId={}, 에러={}", emitterId, e.getMessage());
        // 예외가 발생해도 repository에서는 제거
        emitterRepository.deleteEmitterById(emitterId);
      }
    }

    if (cleanedCount > 0) {
      log.info("기존 연결 정리 완료: userId={}, 정리된 연결 수={}", userId, cleanedCount);
    }
  }

  /**
   * Heartbeat 스케줄링 - 30초마다 ping 전송으로 연결 유지
   */
  private void scheduleHeartbeat(String emitterId, SseEmitter emitter, UUID userId) {
    heartbeatExecutor.scheduleAtFixedRate(() -> {
      try {
        // Repository에서 emitter가 아직 존재하는지 확인
        if (emitterRepository.findAllEmittersByUserIdPrefix(userId.toString()).containsKey(emitterId)) {
          emitter.send("ping");
        }
      } catch (Exception e) {
        log.warn("Heartbeat 전송 실패: emitterId={}, 에러={}", emitterId, e.getMessage());
        // Heartbeat 실패 시 해당 연결을 정리
        handleEmitterError(emitterId, e);
      }
    }, 30, 30, TimeUnit.SECONDS);
  }

  void handleEmitterTermination(String emitterId, String source) {
    safeDeleteEmitter(emitterId);
  }

  void handleEmitterError(String emitterId, Throwable e) {
    log.warn("SSE 오류 발생: emitterId={}, 에러={}", emitterId, e.getMessage());
    safeDeleteEmitter(emitterId);
  }

  /**
   * 안전한 Emitter 삭제 - 예외 처리 포함
   */
  private void safeDeleteEmitter(String emitterId) {
    try {
      emitterRepository.deleteEmitterById(emitterId);
    } catch (Exception e) {
      log.warn("Emitter 삭제 중 오류: emitterId={}, 에러={}", emitterId, e.getMessage());
    }
  }

  // 유저 ID 와 관련된 모든 emitter에게 알림 보냄 (개선된 버전)
  public void sendNotificationToMember(UUID userId, Notification notification) {
    log.info("SSE 알림 전송 시도: userId={}, type={}", userId, notification.getType());

    var emitters = emitterRepository.findAllEmittersByUserIdPrefix(String.valueOf(userId));
    int successCount = 0;
    int failCount = 0;

    for (var entry : emitters.entrySet()) {
      String emitterId = entry.getKey();
      SseEmitter emitter = entry.getValue();

      try {
        // 연결 상태 확인을 위한 간단한 테스트
        if (isEmitterAlive(emitter)) {
          String notificationCacheId = makeNotificationCacheId(emitterId, notification.getId(), userId);
          NotificationDto notificationDto = NotificationDto.from(notification);

          // 캐시에 일시저장
          emitterRepository.saveNotificationCache(notificationCacheId, notification);

          emitter.send(SseEmitter.event()
              .id(notificationCacheId)
              .name(notification.getType().getNotificationName())
              .data(notificationDto));

          successCount++;
        } else {
          log.warn("연결이 끊어진 Emitter 발견: emitterId={}", emitterId);
          safeDeleteEmitter(emitterId);
          failCount++;
        }
      } catch (Exception e) {
        log.warn("SSE 알림 전송 실패: emitterId={}, 에러={}", emitterId, e.getMessage());
        safeDeleteEmitter(emitterId);
        failCount++;
      }
    }

    log.info("알림 전송 완료: userId={}, 성공={}, 실패={}", userId, successCount, failCount);
  }

  /**
   * Emitter 연결 상태 확인
   */
  private boolean isEmitterAlive(SseEmitter emitter) {
    try {
      // 빈 코멘트 전송으로 연결 상태 확인 (클라이언트에는 표시되지 않음)
      emitter.send(SseEmitter.event().comment("connection-check"));
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String makeEmitterId(UUID userId) {
    return userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID();
  }

  private String makeNotificationCacheId(String emitterId, UUID notificationId, UUID userId) {
    return notificationId + "_" + emitterId + "_" + userId;
  }

  // 클라이언트가 Last-Event-ID 헤더를 보내왔는지 여부
  private boolean hasLostData(String lastEventId) {
    return lastEventId != null && !lastEventId.isEmpty();
  }

  private void sendLostData(String lastEventId, UUID userId, SseEmitter emitter) {
    // 임시 보관소에 저장된 유저에 대한 알림을 모두 가져옴
    log.info("누락된 알림 재전송 시작: userId={}, lastEventId={}", userId, lastEventId);

    var notificationCaches = emitterRepository.findAllNotificationCachesByUserIdPrefix(userId);
    int resendCount = 0;

    for (var entry : notificationCaches.entrySet()) {
      String cacheKey = entry.getKey();

      try {
        String[] split = cacheKey.split("_");
        String notificationIdStr = split[0]; // notificationId

        if (lastEventId.compareTo(notificationIdStr) < 0) {
          emitter.send(SseEmitter.event().id(cacheKey).data(entry.getValue()));
          resendCount++;
        }
      } catch (Exception e) {
        log.warn("누락된 알림 재전송 실패: cacheKey={}, 에러={}", cacheKey, e.getMessage());
      }
    }

    log.info("누락된 알림 재전송 완료: userId={}, 재전송 수={}", userId, resendCount);
  }

  public void sendInitNotification(SseEmitter emitter, UUID notificationId, NotificationDto notificationDto) {
    try {
      emitter.send(SseEmitter.event()
          .id(notificationId.toString())
          .name(NotificationType.CONNECTED.getNotificationName())
          .data(notificationDto));
      log.info("초기 연결 알림 전송 완료: notificationId={}", notificationId);
    } catch (Exception e) {
      log.warn("초기 연결 알림 전송 실패: 에러={}", e.getMessage());
    }
  }

  // 모두 읽음 처리된 알림들 각각에 대한 알림캐시 삭제
  public void deleteNotificationCaches(List<Notification> notifications) {
    notifications.forEach(notification -> {
      try {
        emitterRepository.deleteNotificationCachesByNotificationIdPrefix(notification.getId());
      } catch (Exception e) {
        log.warn("알림 캐시 삭제 실패: notificationId={}, 에러={}", notification.getId(), e.getMessage());
      }
    });
  }

  /**
   * 서비스 종료 시 heartbeat executor 정리
   */
  @PreDestroy
  public void cleanup() {
    log.info("EmitterService cleanup - heartbeat executor 종료");
    heartbeatExecutor.shutdown();
    try {
      if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        heartbeatExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      heartbeatExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public void deleteById(UUID userId) {
    log.info("사용자 연결 정리 요청: userId={}", userId);
    cleanupExistingConnections(userId);
  }
}