package team03.mopl.domain.notification.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.notification.service.SseEmitterManager;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;
  private final SseEmitterManager sseEmitterManager;

  /**
   * SSE 구독 연결
   * ResponseEntity나 JSON을 반환하면 SSE 프로토콜이 성립하지 않아서 EventSource나 SSE 클라이언트가 아예 수신을 못 합니다.
   */
  @GetMapping("/subscribe/{userId}")
  public SseEmitter subscribe(@PathVariable UUID userId) {
    return sseEmitterManager.subscribe(userId);
  }

  /**
   * 알림 내역 조회
   */
  @GetMapping("/{userId}")
  public ResponseEntity<List<NotificationDto>> getNotifications(@PathVariable UUID userId) {
    List<NotificationDto> list = notificationService.getNotifications(userId);

    //알림 내역을 조회했다는 건 읽었다는 것
    notificationService.markAllAsRead(userId);

    return ResponseEntity.ok(list);
  }


}
