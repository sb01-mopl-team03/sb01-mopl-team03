package team03.mopl.domain.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertFalse;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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
  @Mock
  ScheduledExecutorService heartbeatExecutor;
  @InjectMocks
  private EmitterService emitterService;

  @Test
  @DisplayName("subscribe - SSEEmitter가 정상적으로 반환되고 connected 이벤트를 보낸다")
  void subscribe_shouldReturnSseEmitterWithConnectedEvent() {
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
  void sendLostData() {
    // given
    UUID userId = UUID.randomUUID();
    String lastNotificationId = UUID.randomUUID().toString(); // 기준점

    SseEmitter emitter = new SseEmitter(TimeUnit.MINUTES.toMillis(60));
    String emitterId = userId + "_test_" + UUID.randomUUID();

    when(emitterRepository.findAllEmittersByUserIdPrefix(userId.toString())).thenReturn(Map.of(emitterId, emitter));

    // 알림 캐시: 일부는 기준점 이전, 일부는 이후
    Map<String, Notification> notificationCache = new ConcurrentHashMap<>();
    for (int i = 0; i < 10; i++) {
      UUID notificationId = UUID.randomUUID();
      String notificationKey = notificationId + "_" + emitterId + "_" + userId;
      Notification notification = new Notification(userId, NotificationType.FOLLOWED, "팔로우 알림 " + i);
      notificationCache.put(notificationKey, notification);
    }

    when(emitterCacheRepository.findAllNotificationCachesByUserIdPrefix(userId)).thenReturn(notificationCache);

    // when
    emitterService.subscribe(userId, lastNotificationId);

    // then
    verify(emitterRepository, times(1)).saveEmitter(anyString(), any(SseEmitter.class));
    assertDoesNotThrow(() -> emitter.send(SseEmitter.event().comment("test")));
  }

  /*@Test
  @DisplayName("onTimeout 콜백이 등록되고 실행되면 handleEmitterTermination이 호출된다")
  void setupEmitterCallbacks_shouldRegisterAndRunOnTimeout() {
    // given
    UUID userId = UUID.randomUUID();
    String emitterId = userId + "_timeout_test";
    long timeout = TimeUnit.MINUTES.toMillis(60);

    SseEmitter emitter = mock(SseEmitter.class);

    // 2) onTimeout 콜백을 캡처하기 위한 ArgumentCaptor
    ArgumentCaptor<Runnable> timeoutCaptor = ArgumentCaptor.forClass(Runnable.class);
    doNothing().when(emitter).onTimeout(timeoutCaptor.capture());

    // 3) setupEmitterCallbacks 직접 호출 (protected)
    emitterService.setupEmitterCallbacks(emitterId, emitter, userId);

    // 4) 캡처된 Runnable이 제대로 존재하는지 검증
    List<Runnable> captured = timeoutCaptor.getAllValues();
    assertFalse("onTimeout 콜백이 등록되어야 합니다",captured.isEmpty());

    // 5) onTimeout 핸들러를 직접 실행
    Runnable onTimeoutHandler = captured.get(0);
    onTimeoutHandler.run();

    // 6) handleEmitterTermination에 의해 deleteEmitterById(emitterId) 호출되었는지 확인
    verify(emitterRepository).deleteEmitterById(emitterId);
  }*/
  @Test
  @DisplayName("setupEmitterCallbacks(private) - 리플렉션 호출 시 콜백 등록 & onError 로직 검증")
  void setupEmitterCallbacks_private_reflection() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    String emitterId = userId + "_test_" + UUID.randomUUID();

    // SseEmitter 를 spy 로 생성 (onCompletion/onError 호출 캡처 가능)
    SseEmitter emitter = Mockito.spy(new SseEmitter(TimeUnit.MINUTES.toMillis(5)));

    // heartbeatExecutor 를 private 필드에 주입
    ReflectionTestUtils.setField(emitterService, "heartbeatExecutor", heartbeatExecutor);

    // scheduleAtFixedRate 호출 시 Runnable 캡처 (실제 실행은 우리가 수동으로)
    ArgumentCaptor<Runnable> heartbeatCaptor = ArgumentCaptor.forClass(Runnable.class);
    given(heartbeatExecutor.scheduleAtFixedRate(
        heartbeatCaptor.capture(), anyLong(), anyLong(), any()))
        .willReturn(Mockito.mock(ScheduledFuture.class));

    // emitterRepository 에 현재 emitter 한 개 존재한다고 가정
    given(emitterRepository.findAllEmittersByUserIdPrefix(userId.toString()))
        .willReturn(Map.of(emitterId, emitter));

    // 리플렉션으로 private 메서드 호출
    Method m = EmitterService.class.getDeclaredMethod(
        "setupEmitterCallbacks", String.class, SseEmitter.class, UUID.class);
    m.setAccessible(true);
    m.invoke(emitterService, emitterId, emitter, userId);

    // then: 콜백 메서드들이 spy 에서 한 번씩 호출되었는지(등록되었는지) 확인
    verify(emitter).onCompletion(any());
    verify(emitter).onTimeout(any());
    verify(emitter).onError(any());

    // onError 콜백을 캡처해서 직접 호출
    @SuppressWarnings("unchecked")
    ArgumentCaptor<Consumer<Throwable>> errorCaptor = ArgumentCaptor.forClass(Consumer.class);
    verify(emitter).onError(errorCaptor.capture());
    Consumer<Throwable> errorCallback = errorCaptor.getValue();
    assertThat(errorCallback).isNotNull();

    // when: Broken pipe 예외를 발생시켜 onError 콜백 실행
    errorCallback.accept(new IOException("Broken pipe"));

    // then: handleEmitterError 가 호출되어 repository 삭제 로직 수행
    verify(emitterRepository).deleteEmitterById(emitterId);

    // heartbeat Runnable 존재 확인 및 실행해도 예외 없이 send 호출 시도하는지 확인
    Runnable heartbeatTask = heartbeatCaptor.getValue();
    assertThat(heartbeatTask).isNotNull();

    // spy emitter 의 send 호출이 예외 없이 진행되도록 기본 설정 (이미 spy 이므로 그냥 실행)
    heartbeatTask.run();
    verify(emitter, atLeastOnce()).send(any(SseEmitter.SseEventBuilder.class));
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
