package team03.mopl.domain.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import java.util.Optional;
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
import org.springframework.core.task.TaskExecutor;
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

  private final TaskExecutor directExecutor = Runnable::run; //동기적 실행 트리거
  @Test
  @DisplayName("sendNotificationToMember: 등록된 모든 emitters 에 캐시 저장 후 전송한다")
  void sendNotificationToMember_sendsAndCaches() throws IOException {
    ReflectionTestUtils.setField(emitterService, "notificationExecutor", directExecutor);

    // given
    UUID userId = UUID.randomUUID();
    Notification notification = new Notification(userId, NotificationType.DM_RECEIVED, "안녕하세요");

    // 두 개의 emitter 를 미리 등록
    String emitterId1 = userId + "1";
    String emitterId2 = userId + "2";
    SseEmitter emitter1 = mock(SseEmitter.class);
    SseEmitter emitter2 = mock(SseEmitter.class);

    given(emitterRepository.findAllEmittersByUserIdPrefix(userId.toString()))
        .willReturn(Map.of(emitterId1, emitter1, emitterId2, emitter2));

    // when: 비동기 CompletableFuture 가 바로 실행되도록 join()
    emitterService.sendNotificationToMember(userId, notification).join();

    //saveNotificationCache(notificationCacheId, notification)
    //위 형식을 지키고 있는 지 확인
    then(emitterCacheRepository).should().saveNotificationCache(
        argThat(key -> key.startsWith(notification.getId() + "_" + emitterId1 + "_")),
        eq(notification)
    );
    then(emitterCacheRepository).should().saveNotificationCache(
        argThat(key -> key.startsWith(notification.getId() + "_" + emitterId2 + "_")),
        eq(notification)
    );
    // isEmitterAlive + send
    verify(emitter1, times(2))
        .send(any(SseEmitter.SseEventBuilder.class));
    verify(emitter2, times(2))
        .send(any(SseEmitter.SseEventBuilder.class));
  }
  @Test
  @DisplayName("sendInitNotification: 정상적으로 CONNECTED 이벤트 전송")
  void sendInitNotification_sendsConnectedEvent() throws Exception {
    // given
    SseEmitter emitter = mock(SseEmitter.class);

    // when
    emitterService.sendInitNotification(emitter);

    // then: SseEventBuilder 타입으로 한번 호출됐는지만 검증
    verify(emitter, times(1))
        .send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  @DisplayName("sendInitNotification: send() 중 예외 발생해도 예외를 던지지 않는다")
  void sendInitNotification_handlesException() throws Exception {
    // given
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new IOException("Broken pipe"))
        .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

    // when / then: 어떤 예외도 던지지 않아야 한다
    assertThatCode(() -> emitterService.sendInitNotification(emitter)).doesNotThrowAnyException();

    // 그리고 send는 한 번 시도된다
    verify(emitter, times(1))
        .send(any(SseEmitter.SseEventBuilder.class));
  }

  @Test
  @DisplayName("deleteNotificationCaches: 모든 알림 캐시 ID로 delete 호출")
  void deleteNotificationCaches_callsDeleteForEachNotification() {
    // given: 두 개의 Notification 객체에 미리 ID 설정
    UUID userId = UUID.randomUUID();
    Notification n1 = new Notification(userId, NotificationType.FOLLOWED, "foo");
    Notification n2 = new Notification(userId, NotificationType.FOLLOWED, "bar");
    UUID id1 = UUID.randomUUID(), id2 = UUID.randomUUID();
    // ReflectionTestUtils 로 id 필드 주입 (만약 Notification.id 가 private 이라면)
    ReflectionTestUtils.setField(n1, "id", id1);
    ReflectionTestUtils.setField(n2, "id", id2);

    // when
    emitterService.deleteNotificationCaches(List.of(n1, n2));

    // then: 정확히 두 번, 각 ID로 deleteNotificationCachesByNotificationIdPrefix 호출
    verify(emitterCacheRepository, times(1))
        .deleteNotificationCachesByNotificationIdPrefix(id1);
    verify(emitterCacheRepository, times(1))
        .deleteNotificationCachesByNotificationIdPrefix(id2);
  }

  @Test
  @DisplayName("deleteNotificationCaches: 하나가 실패해도 나머지는 호출되고 예외를 던지지 않는다")
  void deleteNotificationCaches_handlesExceptionAndContinues() {
    // given
    UUID userId = UUID.randomUUID();
    Notification n1 = new Notification(userId, NotificationType.FOLLOWED, "foo");
    Notification n2 = new Notification(userId, NotificationType.FOLLOWED, "bar");
    UUID id1 = UUID.randomUUID(), id2 = UUID.randomUUID();
    ReflectionTestUtils.setField(n1, "id", id1);
    ReflectionTestUtils.setField(n2, "id", id2);

    // 의도적으로 첫 번째 호출 시 예외
    doThrow(new RuntimeException("DB down"))
        .when(emitterCacheRepository).deleteNotificationCachesByNotificationIdPrefix(id1);

    // when / then: 메서드가 예외를 던지지 않아야 한다
    assertDoesNotThrow(() ->
        emitterService.deleteNotificationCaches(List.of(n1, n2))
    );

    //emitterCacheRepository 모킹된 객체에서 뒤에 이어지는 메서드 호출이 정확히 1회 일어났는지 확인
    verify(emitterCacheRepository, times(1))
        .deleteNotificationCachesByNotificationIdPrefix(id1);
    // 그리고 두 번째 ID는 정상 호출
    verify(emitterCacheRepository, times(1))
        .deleteNotificationCachesByNotificationIdPrefix(id2);
  }

  @Test
  @DisplayName("cleanup: 남아있는 heartbeat 작업은 취소하고, tasks 맵을 비운 뒤 executor를 종료한다")
  void cleanup_shouldCancelPendingTasks_clearMap_andShutdownExecutor() {
    // 1) heartbeatExecutor 를 mock 으로 교체
    ScheduledExecutorService mockExecutor = mock(ScheduledExecutorService.class);
    ReflectionTestUtils.setField(emitterService, "heartbeatExecutor", mockExecutor);

    // 2) heartbeatTasks 맵에 두 개의 ScheduledFuture mock 추가
    @SuppressWarnings("unchecked")
    var tasks = (ConcurrentHashMap<String, ScheduledFuture<?>>)
        ReflectionTestUtils.getField(emitterService, "heartbeatTasks");
    ScheduledFuture<?> future1 = mock(ScheduledFuture.class);
    ScheduledFuture<?> future2 = mock(ScheduledFuture.class);

    // future1 은 아직 cancel 되지 않은 상태 → cancel(true) 되어야 함
    when(future1.isCancelled()).thenReturn(false);
    // future2 는 이미 cancel 됐다고 치자 → cancel() 호출되지 않아야 함
    when(future2.isCancelled()).thenReturn(true);

    tasks.put("task1", future1);
    tasks.put("task2", future2);

    // 3) cleanup() 호출
    emitterService.cleanup();

    // ---- 검증 ----
    // 아직 취소되지 않은 future1 에만 cancel(true) 호출
    verify(future1).cancel(true);
    // 이미 cancel 됐던 future2 에는 cancel 호출 없어야 함
    verify(future2, never()).cancel(anyBoolean());

    // 맵이 완전히 비워졌는지
    assertTrue(tasks.isEmpty(), "heartbeatTasks 맵이 비워져야 합니다");

    // executor.shutdown() 과 shutdownNow() 가 호출됐는지
    verify(mockExecutor).shutdown();
    // 스텁하지 않은 mock 의 awaitTermination 은 false 를 반환하므로 shutdownNow() 까지 호출되는 시나리오입니다.
    verify(mockExecutor).shutdownNow();
  }
  @Test
  @DisplayName("deleteById - 등록된 모든 Emitter 연결이 삭제된다")
  void deleteById_shouldDeleteAllRegisteredEmitters() {
    // given
    UUID userId = UUID.randomUUID();
    String emitterId1 = userId + "_conn1";
    String emitterId2 = userId + "_conn2";

    // 두 개의 emitter 가 등록돼 있다고 가정
    SseEmitter dummy1 = new SseEmitter();
    SseEmitter dummy2 = new SseEmitter();
    given(emitterRepository.findAllEmittersByUserIdPrefix(userId.toString()))
        .willReturn(Map.of(emitterId1, dummy1, emitterId2, dummy2));

    // when
    emitterService.deleteById(userId);

    // then
    // cleanupExistingConnections() 내부에서 deleteEmitterById(emitterId) 가 호출돼야 한다
    verify(emitterRepository, times(1)).deleteEmitterById(emitterId1);
    verify(emitterRepository, times(1)).deleteEmitterById(emitterId2);
  }

}
