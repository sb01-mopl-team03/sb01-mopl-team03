package team03.mopl.domain.notification.service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.repository.EmitterCacheRepository;
import team03.mopl.domain.notification.repository.EmitterRepository;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtBlacklist;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmitterService {

  private final EmitterRepository emitterRepository;
  private final EmitterCacheRepository emitterCacheRepository;
  private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(5);
  private final TaskExecutor notificationExecutor;
  private final ScheduledExecutorService retryScheduler = Executors.newScheduledThreadPool(2);

  private final JwtProvider jwtProvider;
  private final JwtBlacklist jwtBlacklist;

  // 각 emitter의 heartbeat 작업을 추적하기 위한 맵
  private final ConcurrentHashMap<String, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();

  public SseEmitter subscribe(UUID userId, String lastNotificationId ) {

    // 기존 연결 정리 먼저 수행
    cleanupExistingConnections(userId);

    // 한 계정이 여러 기기로 접속할 수 있기에 같은 유저라도 다른 ID 값 필요
    String emitterId = makeEmitterId(userId);
    log.info("subscribe - SSE 구독 시작: userId = {}, emitterId = {}", userId, emitterId);

    // 타임아웃을 5분으로 설정 (토큰 갱신 주기를 고려)
    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(60));

    // Repository에 저장하기 전에 emitter 설정 완료
    setupEmitterCallbacks(emitterId, emitter, userId);
    emitterRepository.saveEmitter(emitterId, emitter);

    // 미수신 데이터 재전송
    if (hasLostData(lastNotificationId)) {
      log.info("SSE 재연결 - 누락된 데이터 복원 시도: lastNotificationId = {}", lastNotificationId);
      sendLostData(lastNotificationId, userId, emitter);
    }

    try {
      emitter.send(SseEmitter.event()
          .name("connected")
          .data("Connection established"));
    } catch (IOException e) {
      log.warn("초기 연결 메시지 전송 실패: userId={}", userId);
    }

    return emitter;
  }

  /**
   * Emitter 콜백 설정
   */
  private void setupEmitterCallbacks(String emitterId, SseEmitter emitter, UUID userId) {
    // 연결 종료, 타임아웃, 에러 시 콜백 등록
    emitter.onCompletion(() -> {
      log.debug("SSE 연결 정상 완료: emitterId = {}", emitterId);
      handleEmitterTermination(emitterId, "onCompletion");
    });

    emitter.onTimeout(() -> {
      log.debug("SSE 연결 타임아웃: emitterId = {}", emitterId);
      handleEmitterTermination(emitterId, "onTimeout");
    });

    emitter.onError(e -> {
      // 에러 타입에 따라 로그 레벨 조정
      if (e instanceof IOException && e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
        log.debug("SSE 클라이언트 연결 끊김 (정상): emitterId = {}", emitterId);
      } else {
        log.warn("SSE 연결 에러: emitterId = {}, 에러 = {}", emitterId, e.getMessage());
      }
      handleEmitterError(emitterId, e);
    });

    // Heartbeat 스케줄링 (45초마다 - 토큰 검증 포함)
    scheduleHeartbeatWithTokenValidation(emitterId, emitter, userId);
  }

  /**
   * 기존 연결 정리 - 동일 사용자의 이전 연결들을 안전하게 종료
   */
  private void cleanupExistingConnections(UUID userId) {
    var existingEmitters = emitterRepository.findAllEmittersByUserIdPrefix(userId.toString());
    int cleanedCount = 0;

    for (var entry : existingEmitters.entrySet()) {
      String emitterId = entry.getKey();
      try {
        cancelHeartbeatTask(emitterId);
        // safelyCloseEmitterWithCheck(emitter); // ← 이거 제거!
        emitterRepository.deleteEmitterById(emitterId);
      } catch (Exception e) {
        // 예외 무시하고 계속 진행
      }
    }
  }

  /**
   * 더 안전한 Emitter 종료 - 상태 확인 포함
   */
 /* private void safelyCloseEmitterWithCheck(SseEmitter emitter) {
    if (emitter == null) {
      return;
    }

    try {
      // 매우 간단한 코멘트로 연결 상태 확인
      emitter.send(SseEmitter.event().comment(""));

      // 연결이 살아있다면 정상 종료
      emitter.send(SseEmitter.event()
          .name("connection-closed")
          .data("New connection established"));
      emitter.complete();

      log.debug("Emitter 정상 종료 완료");

    } catch (IllegalStateException e) {
      // 이미 완료되었거나 에러 상태
      log.debug("Emitter 이미 종료된 상태: {}", e.getMessage());
    } catch (Exception e) {
      // 연결이 끊어진 상태 - 리소스만 정리
      log.debug("Emitter 연결 끊어짐, 리소스 정리: {}", e.getMessage());
    }
  }*/

  /**
   * 안전한 Emitter 종료 - 응답 상태 확인 후 처리
   */
  /*private void safelyCloseEmitter(SseEmitter emitter, String message) {
    try {
      // 먼저 간단한 테스트 메시지를 보내서 연결 상태 확인
      emitter.send(SseEmitter.event().comment("connection-test"));

      // 연결이 살아있다면 종료 메시지 전송
      emitter.send(SseEmitter.event()
          .name("connection-closed")
          .data(message));

      emitter.complete();

    } catch (IllegalStateException e) {
      // 이미 완료되었거나 에러 상태인 경우
      log.debug("Emitter 이미 종료됨: {}", e.getMessage());
    } catch (Exception e) {
      // 기타 연결 문제 - 강제 종료 시도
      log.debug("Emitter 종료 중 예외, 강제 종료 시도: {}", e.getMessage());
      try {
        emitter.completeWithError(e);
      } catch (Exception ex) {
        log.debug("강제 종료도 실패 (정상): {}", ex.getMessage());
      }
    }
  }*/

  /**
   * 토큰 검증을 포함한 Heartbeat 스케줄링
   */
  private void scheduleHeartbeatWithTokenValidation(String emitterId, SseEmitter emitter, UUID userId) {
    ScheduledFuture<?> heartbeatTask = heartbeatExecutor.scheduleAtFixedRate(() -> {
      try {
        // Repository에서 emitter가 아직 존재하는지 확인
        if (!emitterRepository.findAllEmittersByUserIdPrefix(userId.toString()).containsKey(emitterId)) {
          log.debug("Emitter가 더 이상 존재하지 않음: emitterId = {}", emitterId);
          cancelHeartbeatTask(emitterId);
          return;
        }

        // 정상적인 heartbeat 전송 (안전하게)
        sendHeartbeatSafely(emitter, emitterId);

      } catch (Exception e) {
        log.warn("Heartbeat 전송 실패: emitterId = {}, 에러 = {}", emitterId, e.getMessage());
        handleEmitterError(emitterId, e);
      }
    }, 45, 45, TimeUnit.SECONDS); // 45초마다 실행

    // 작업 추적을 위해 저장
    heartbeatTasks.put(emitterId, heartbeatTask);
  }

  /**
   * 안전한 Heartbeat 전송 - 연결 상태 확인 포함
   */
  private void sendHeartbeatSafely(SseEmitter emitter, String emitterId) {
    try {
      // 매우 가벼운 heartbeat 전송
      emitter.send(SseEmitter.event().name("heartbeat").data("ping"));

    } catch (IllegalStateException e) {
      // Emitter가 이미 완료되었거나 에러 상태
      log.debug("Heartbeat 실패 - Emitter 이미 종료됨: emitterId = {}", emitterId);
      throw new RuntimeException("Emitter already completed", e);

    } catch (IOException e) {
      // 연결이 끊어짐 (Broken pipe 등)
      log.debug("Heartbeat 실패 - 연결 끊어짐: emitterId = {}, 에러 = {}", emitterId, e.getMessage());
      throw new RuntimeException("Connection broken", e);

    } catch (Exception e) {
      // 기타 예외
      log.warn("Heartbeat 실패 - 기타 오류: emitterId = {}, 에러 = {}", emitterId, e.getMessage());
      throw new RuntimeException("Heartbeat failed", e);
    }
  }

  /**
   * 토큰 만료 처리
   */
  private void handleTokenExpired(String emitterId, SseEmitter emitter) {
    try {
      // 토큰 만료 알림 전송
      emitter.send(SseEmitter.event()
          .name("token-expired")
          .data("Token expired, please reconnect"));

      // 정상 종료
      emitter.complete();
    } catch (Exception e) {
      log.warn("토큰 만료 알림 전송 실패: emitterId = {}", emitterId);
    } finally {
      // 리소스 정리
      cancelHeartbeatTask(emitterId);
      safeDeleteEmitter(emitterId);
    }
  }

  /**
   * Heartbeat 작업 취소
   */
  private void cancelHeartbeatTask(String emitterId) {
    ScheduledFuture<?> task = heartbeatTasks.remove(emitterId);
    if (task != null && !task.isCancelled()) {
      task.cancel(true);
      log.debug("Heartbeat 작업 취소: emitterId = {}", emitterId);
    }
  }

  void handleEmitterTermination(String emitterId, String source) {
    log.debug("SSE 연결 종료: emitterId = {}, source = {}", emitterId, source);
    cancelHeartbeatTask(emitterId);
    safeDeleteEmitter(emitterId);
  }

  void handleEmitterError(String emitterId, Throwable e) {
    log.warn("SSE 오류 발생: emitterId = {}, 에러 = {}", emitterId, e.getMessage());
    cancelHeartbeatTask(emitterId);
    safeDeleteEmitter(emitterId);
  }

  /**
   * 안전한 Emitter 삭제 - 예외 처리 포함
   */
  private void safeDeleteEmitter(String emitterId) {
    try {
      emitterRepository.deleteEmitterById(emitterId);
    } catch (Exception e) {
      log.warn("Emitter 삭제 중 오류: emitterId = {}, 에러 = {}", emitterId, e.getMessage());
    }
  }

  public CompletableFuture<Void> sendNotificationToMember(UUID userId, Notification notification) {
    return sendNotificationWithRetry(userId, notification, 1);
  }

  // 유저 ID 와 관련된 모든 emitter에게 알림 보냄 (개선된 버전)
  public CompletableFuture<Void> sendNotificationWithRetry(UUID userId, Notification notification, int attempt) {
    return CompletableFuture.runAsync(() -> {
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
                emitterCacheRepository.saveNotificationCache(notificationCacheId, notification);

                emitter.send(SseEmitter.event().id(notificationCacheId).name(notification.getType().getNotificationName()).data(notificationDto));

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
        }, notificationExecutor).orTimeout(2, TimeUnit.SECONDS) // 타임아웃 설정
        .exceptionally(ex -> {
          if (attempt < 3) {
            log.warn("알림 전송 실패 - 재시도 예정: attempt={}, userId={}, error={}", attempt, userId, ex.getMessage());

            retryScheduler.schedule(() -> {
              sendNotificationWithRetry(userId, notification, attempt + 1);
            }, 200, TimeUnit.MILLISECONDS);

          } else {
            log.error("최종 실패: userId={}, 에러={}", userId, ex.getMessage());
          }
          return null;
        });
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
    log.info("누락된 알림 재전송 시작: userId = {}, lastEventId = {}", userId, lastEventId);

    var notificationCaches = emitterCacheRepository.findAllNotificationCachesByUserIdPrefix(userId);
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
        //전송 성공하면 캐시 비우는 로직 추가??
      } catch (Exception e) {
        log.warn("누락된 알림 재전송 실패: cacheKey = {}, 에러 = {}", cacheKey, e.getMessage());
      }
    }

    log.info("누락된 알림 재전송 완료: userId = {}, 재전송 수 = {}", userId, resendCount);
  }

  public void sendInitNotification(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event()
          .name(NotificationType.CONNECTED.getNotificationName())
          .data("연결됨"));
      log.info("초기 연결 알림 전송 완료");
    } catch (Exception e) {
      log.warn("초기 연결 알림 전송 실패: 에러={}", e.getMessage());
    }
  }

  // 모두 읽음 처리된 알림들 각각에 대한 알림캐시 삭제
  public void deleteNotificationCaches(List<Notification> notifications) {
    notifications.forEach(notification -> {
      try {
        emitterCacheRepository.deleteNotificationCachesByNotificationIdPrefix(notification.getId());
      } catch (Exception e) {
        log.warn("알림 캐시 삭제 실패: notificationId = {}, 에러 = {}", notification.getId(), e.getMessage());
      }
    });
  }

  /**
   * 서비스 종료 시 heartbeat executor 정리
   */
  @PreDestroy
  public void cleanup() {
    log.info("EmitterService cleanup 시작");

    // 모든 heartbeat 작업 취소
    heartbeatTasks.values().forEach(task -> {
      if (task != null && !task.isCancelled()) {
        task.cancel(true);
      }
    });
    heartbeatTasks.clear();

    // heartbeat executor 종료
    heartbeatExecutor.shutdown();
    try {
      if (!heartbeatExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        heartbeatExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      heartbeatExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }

    log.info("EmitterService cleanup 완료");
  }

  public void deleteById(UUID userId) {
    log.info("사용자 연결 정리 요청: userId = {}", userId);
    cleanupExistingConnections(userId);
  }
}
