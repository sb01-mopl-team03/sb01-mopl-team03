package team03.mopl.domain.notification.service;

import java.util.List;
import java.util.UUID;
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
  //private final NotificationService notificationService;

  public SseEmitter subscribe(UUID userId, String lastNotificationId) {
    // 한 계정이 여러 기기로 접속할 수 있기에 같은 유저라도 다른 ID 값 필요
    String emitterId = makeEmitterId(userId);
    log.info("subscribe - SSE 구독 시작: userId={}, emitterId={}, lastNotificationId={}", userId, emitterId, lastNotificationId);

    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(10));
    emitterRepository.saveEmitter(emitterId, emitter);

    // 연결 종료, 타임아웃, 에러 시 콜백 등록
    emitter.onCompletion(() -> handleEmitterTermination(emitterId, "onCompletion"));
    emitter.onTimeout(() -> handleEmitterTermination(emitterId, "onTimeout"));
    emitter.onError(e -> handleEmitterError(emitterId, e));

    // 미수신 데이터 재전송
    // 연결이 일시적으로 끊기고 나서 클라이언트는 SSE 재연결 시도 후 Last-Event-ID를 헤더로 함께 보냄
    // 서버는 마지막으로 받은 이벤트 ID를 알게되고 이후부터 알림 다시 보냄
    if (hasLostData(lastNotificationId)) {
      log.info("subscribe - SSE 재연결 - 누락된 데이터 복원 시도: lastNotificationId={}", lastNotificationId);
      sendLostData(lastNotificationId, userId, emitter);
    }

    return emitter;
  }
  void handleEmitterTermination(String emitterId, String source) {
    log.info("subscribe - SSE {} 호출: emitterId={}", source, emitterId);
    emitterRepository.deleteEmitterById(emitterId);
  }

  void handleEmitterError(String emitterId, Throwable e) {
    log.warn("subscribe - SSE 오류 발생: emitterId={}, 에러={}", emitterId, e.getMessage());
    emitterRepository.deleteEmitterById(emitterId);
  }

  // 유저 ID 와 관련된 모든 emitter에게 알림 보냄
  public void sendNotificationToMember(UUID userId, Notification notification) {
    log.info("sendNotificationToMember - SSE 알림 전송 시도: userId={}, notificationId={}, type={}",
        userId, notification.getId(), notification.getType());
    emitterRepository.findAllEmittersByUserIdPrefix(String.valueOf(userId))
        .forEach((emitterId, emitter) -> {
          try {
            // 같은 내용의 알림이 한 유저의 다양한 기기에 전송
            String notificationCacheId = makeNotificationCacheId(emitterId, notification.getId(), userId);
            NotificationDto notificationDto = NotificationDto.from(notification);

            //캐시에 일시저장
            emitterRepository.saveNotificationCache(notificationCacheId, notification);

            emitter.send(SseEmitter.event()
                .id(notificationCacheId)
                .name(notification.getType().getNotificationName())
                .data(notificationDto));
            log.info("sendNotificationToMember - SSE 알림 전송 성공: emitterId={}, notificationCacheId={}", emitterId, notificationCacheId);
          } catch (Exception e) {
            //전송 실패한 emiiter
            //보통 클라이언트(브라우저)가 연결을 끊었거나 네트워크가 끊긴 상태
            //로깅 필요
            log.warn("sendNotificationToMember - SSE 알림 전송 실패: emitterId={}, 에러={}", emitterId, e.getMessage());
            emitterRepository.deleteEmitterById(emitterId);
          }
        });
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
    // 임시 보관소에 저장된 유저에 대한 알림을 모두 가져옴 ( 유저에 대한 전체 알림이 아님 )
    log.info("sendLostData - 누락된 알림 재전송 시작: userId={}, lastEventId={}", userId, lastEventId);
    emitterRepository.findAllEmittersByUserIdPrefix(String.valueOf(userId))
        .entrySet().stream()
        //notification Id를 기준으로 비교
        .filter(entry -> {
          String[] split = entry.getKey().split("_");
          String notificationIdStr = split[0]; // notificationId
          return lastEventId.compareTo(notificationIdStr) < 0;
        })
        .forEach(entry -> {
          try {
            emitter.send(SseEmitter.event().id(entry.getKey()).data(entry.getValue()));
            log.info("sendLostData - 누락된 알림 재전송 성공: emitterId={}", entry.getKey());
          } catch (Exception e) {
            log.warn("sendLostData - 누락된 알림 재전송 실패: emitterId={}, 에러={}", entry.getKey(), e.getMessage());
            emitterRepository.deleteEmitterById(entry.getKey());
          }
        });
  }

  public void sendInitNotification(SseEmitter emitter, UUID notificationId, NotificationDto notificationDto) {
    try {
      emitter.send(SseEmitter.event()
          .id(notificationId.toString())
          .name(NotificationType.CONNECTED.getNotificationName())
          .data(notificationDto));
      log.info("sendInitNotification - 초기 연결 알림 전송 완료: notificationId={}", notificationId);
    } catch (Exception e) {
      log.warn("sendInitNotification - 초기 연결 알림 전송 실패: 에러={}", e.getMessage());
    }
  }


  // 모두 읽음 처리된 알림들 각각에 대한 알림캐시 삭제 ( 알림 캐시 또한 emitter 마다 존재 )
  public void deleteNotificationCaches(List<Notification> notifications) {
    notifications.forEach(notification -> {
      emitterRepository.deleteNotificationCachesByNotificationIdPrefix(notification.getId());
      log.info("deleteNotificationCaches - 알림 캐시 삭제 완료: notificationId={}", notification.getId());
    });
  }

}

