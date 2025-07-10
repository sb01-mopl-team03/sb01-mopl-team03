package team03.mopl.domain.notification.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
  @GetMapping("/subscribe")
  public SseEmitter subscribe(@AuthenticationPrincipal CustomUserDetails user,
      @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {
    UUID userId = user.getId();
    log.debug("알림 SSE 구독 요청: userId={}, lastEventId={}", userId, lastEventId);

    // 1. 연결된 사실을 알림으로 저장
    NotificationDto notificationDto = new NotificationDto(userId, NotificationType.CONNECTED, "SSE 연결 완료");
    UUID connectedNotificationId = notificationService.sendNotification(notificationDto);

    // 2. SSEEmitter 구독
    SseEmitter emitter = emitterService.subscribe(userId, lastEventId);

    // 3. 구독 직후, CONNECTED 알림 전송
    emitterService.sendInitNotification(emitter, connectedNotificationId, notificationDto);

    return emitter;
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


}
