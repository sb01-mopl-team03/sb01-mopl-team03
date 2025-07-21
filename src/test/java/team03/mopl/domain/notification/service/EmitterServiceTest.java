package team03.mopl.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.repository.EmitterCacheRepository;
import team03.mopl.domain.notification.repository.EmitterRepository;

@ExtendWith(MockitoExtension.class)
class EmitterServiceTest {

  @Mock
  private EmitterRepository emitterRepository;

  @Mock
  private EmitterCacheRepository emitterCacheRepository;

  @InjectMocks
  private EmitterService emitterService;

  @Test
  @DisplayName("subscribe - SSEEmitter가 정상적으로 반환되고 connected 이벤트를 보낸다")
  void subscribe_shouldReturnSseEmitterWithConnectedEvent() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    String lastNotificationId = null;
    when(emitterRepository.findAllEmittersByUserIdPrefix(userId.toString())).thenReturn(Map.of());
    SseEmitter emitter = emitterService.subscribe(userId, lastNotificationId);

    // then
    assertNotNull(emitter);
    assertDoesNotThrow(() -> emitter.send(SseEmitter.event().comment("test")));
    verify(emitterRepository).saveEmitter(anyString(), eq(emitter));
  }
  @Test
  @DisplayName("sendLostData - 누락된 알림만 재전송")
  void sendLostData() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    String lastNotificationId = UUID.randomUUID().toString(); // 기준점

    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(60));
    String emitterId = userId + "_test_" + UUID.randomUUID();

    when(emitterRepository.findAllEmittersByUserIdPrefix(userId.toString()))
        .thenReturn(Map.of(emitterId, emitter));

    // 알림 캐시: 일부는 기준점 이전, 일부는 이후
    Map<String, Notification> notificationCache = new ConcurrentHashMap<>();
    for (int i = 0; i < 10; i++) {
      UUID notificationId = UUID.randomUUID();
      String notificationKey = notificationId + "_" + emitterId + "_" + userId;
      Notification notification = new Notification(userId, NotificationType.FOLLOWED, "팔로우 알림 " + i);
      notificationCache.put(notificationKey, notification);
    }

    when(emitterCacheRepository.findAllNotificationCachesByUserIdPrefix(userId))
        .thenReturn(notificationCache);

    // when
    emitterService.subscribe(userId, lastNotificationId);

    // then
    verify(emitterRepository, times(1)).saveEmitter(anyString(), any(SseEmitter.class));
    assertDoesNotThrow(() -> emitter.send(SseEmitter.event().comment("test")));
  }

  @Test
  void handleEmitterTermination_shouldDeleteEmitter() {
    emitterService.handleEmitterTermination("emitter-id-123", "onCompletion");
    verify(emitterRepository).deleteEmitterById("emitter-id-123");
  }

  @Test
  void handleEmitterError_shouldDeleteEmitterAndLog() {
    emitterService.handleEmitterError("emitter-id-456", new RuntimeException("fail"));
    verify(emitterRepository).deleteEmitterById("emitter-id-456");
  }


}
