package team03.mopl.domain.notification.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.dto.NotificationPagingDto;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.EmitterService;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

  private final NotificationService notificationService;
  private final EmitterService emitterService;

  /**
   * SSE 구독 연결
   * SSE 구독 연결 ResponseEntity나 JSON을 반환하면 SSE 프로토콜이 성립하지 않아서 EventSource나 SSE 클라이언트가 아예 수신을 못 합니다.
   */
  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails user,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId,
      HttpServletResponse response) {

    log.info("=== SSE 구독 요청 시작 ===");
    log.info("User 정보: {}", user);
    log.info("Last-Event-ID: {}", lastEventId);

    SseEmitter emitter = null;

    try {
      // 1. 사용자 인증 확인
      if (user == null) {
        log.error("인증되지 않은 사용자의 SSE 구독 시도");
        emitter = new SseEmitter();
        emitter.completeWithError(new RuntimeException("Unauthorized: 인증이 필요합니다"));
        return emitter;
      }

      UUID userId = user.getId();
      log.info("알림 SSE 구독 요청: userId={}, lastEventId={}", userId, lastEventId);

      // 2. SSEEmitter 구독 (먼저 생성)
      log.debug("SSE Emitter 구독 중...");
      emitter = emitterService.subscribe(userId, lastEventId);

      // 3. 에러 핸들러 설정 (Broken pipe 에러 처리)
      emitter.onCompletion(() -> {
        log.info("SSE 연결이 정상적으로 완료되었습니다: userId={}", userId);
      });

      emitter.onTimeout(() -> {
        log.info("SSE 연결이 타임아웃되었습니다: userId={}", userId);
        emitterService.deleteById(userId); // emitter 정리
      });

      emitter.onError((ex) -> {
        if (ex instanceof IOException && ex.getMessage().contains("Broken pipe")) {
          log.debug("클라이언트가 연결을 끊었습니다: userId={}", userId);
        } else {
          log.error("SSE 연결 오류: userId={}, error={}", userId, ex.getMessage());
        }
        emitterService.deleteById(userId); // emitter 정리
      });

      log.debug("SSE Emitter 구독 완료");

      // 4. 구독 직후, CONNECTED 알림 전송
      log.debug("초기 알림 전송 중...");
      emitterService.sendInitNotification(emitter);
      log.info("SSE 구독 완료: userId={}", userId);

      return emitter;

    } catch (Exception e) {
      log.error("SSE 구독 중 예외 발생: userId={}, error={}",
          user != null ? user.getId() : "null", e.getMessage(), e);

      // 이미 생성된 emitter가 있다면 에러로 완료
      if (emitter != null) {
        try {
          emitter.completeWithError(e);
        } catch (Exception completeError) {
          log.error("Emitter 에러 완료 중 예외 발생: {}", completeError.getMessage());
        }
        return emitter;
      }

      // emitter가 없다면 새로 생성해서 에러 반환
      try {
        SseEmitter errorEmitter = new SseEmitter();
        errorEmitter.completeWithError(e);
        return errorEmitter;
      } catch (Exception createError) {
        log.error("에러 Emitter 생성 중 예외 발생: {}", createError.getMessage());
        // 최후의 수단으로 빈 emitter 반환
        return new SseEmitter();
      }
    }
  }

  /**
   * 알림 내역 조회
   */
  @GetMapping
  public ResponseEntity<CursorPageResponseDto<NotificationDto>> getNotifications(@Valid @ModelAttribute NotificationPagingDto notificationPagingDto,
      @AuthenticationPrincipal CustomUserDetails user) {
    UUID userId = user.getId();
    log.info("알림 내역 조회 요청: userId={}", userId);
    CursorPageResponseDto<NotificationDto> list = notificationService.getNotifications(notificationPagingDto, userId);

    //알림 내역을 조회했다는 건 읽었다는 것
    notificationService.markAllAsRead(userId);
    log.info("알림 읽음 처리 완료: userId={}, 알림 수={}", userId, list.size());
    return ResponseEntity.ok(list);
  }

  @DeleteMapping("/{notificationId}")
  public ResponseEntity<Void> deleteNotification(@PathVariable("notificationId") UUID notificationId, @AuthenticationPrincipal CustomUserDetails user) {
    notificationService.deleteNotification(notificationId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping
  public ResponseEntity<Void> deleteNotificationByUserId(@AuthenticationPrincipal CustomUserDetails user) {
    UUID authenticatedUserId = user.getId();
    notificationService.deleteNotificationByUserId(authenticatedUserId);
    return ResponseEntity.noContent().build();
  }

}
