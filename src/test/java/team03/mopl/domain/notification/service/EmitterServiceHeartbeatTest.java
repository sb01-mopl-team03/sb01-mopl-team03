package team03.mopl.domain.notification.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import team03.mopl.domain.notification.repository.EmitterRepository;

@ExtendWith(MockitoExtension.class)
class EmitterServiceHeartbeatTest {

  @Mock
  EmitterRepository emitterRepository;


  @InjectMocks
  private EmitterService emitterService;

  @Test
  void scheduleHeartbeatWithTokenValidation_sendsHeartbeat() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    String emitterId = userId + "_test_" + UUID.randomUUID();
    SseEmitter emitter = mock(SseEmitter.class);

    given(emitterRepository.findAllEmittersByUserIdPrefix(userId.toString())).willReturn(Map.of(emitterId, emitter));

    // 테스트용 ScheduledExecutorService를 spy/mock
    ScheduledExecutorService testExecutor = mock(ScheduledExecutorService.class);

    // private 필드인 heartbeatExecutor 교체
    ReflectionTestUtils.setField(emitterService, "heartbeatExecutor", testExecutor);

    // scheduleAtFixedRate 호출 시 Runnable만 캡처
    ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
    when(testExecutor
        .scheduleAtFixedRate(captor.capture(), eq(45L), eq(45L), eq(TimeUnit.SECONDS)))
        .thenReturn(mock(ScheduledFuture.class)
    );

    // when: private 메서드 reflection 호출
    Method m = EmitterService.class.getDeclaredMethod("scheduleHeartbeatWithTokenValidation", String.class, SseEmitter.class, UUID.class);
    m.setAccessible(true);
    m.invoke(emitterService, emitterId, emitter, userId);

    // then
    Runnable heartbeatTask = captor.getValue();
    assertNotNull(heartbeatTask, "heartbeat Runnable이 등록되어야 합니다");
    heartbeatTask.run();

    // 이제 send() 호출 여부를 검증
    verify(emitter).send(isA(SseEmitter.SseEventBuilder.class));


  }

  private Method sendHeartbeatMethod() throws NoSuchMethodException {
    Method m = EmitterService.class
        .getDeclaredMethod("sendHeartbeatSafely", SseEmitter.class, String.class);
    m.setAccessible(true);
    return m;
  }

  @Test
  @DisplayName("sendHeartbeatSafely - Emitter already completed 시 IllegalStateException 을 RuntimeException으로 래핑한다")
  void whenEmitterCompleted_thenRuntimeExceptionWithEmitterAlreadyCompleted() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    // send() 호출 시 IllegalStateException 발생
    doThrow(new IllegalStateException("Emitter already completed")).when(emitter).send(any(SseEmitter.SseEventBuilder.class));

    Method m = sendHeartbeatMethod();
    //실제 던져진 예외를 꺼내서 메시지 내용 확인
    InvocationTargetException ite = assertThrows(
        InvocationTargetException.class,
        () -> m.invoke(emitterService, emitter, "em1")
    );

    Throwable rte = ite.getTargetException();
    assertInstanceOf(RuntimeException.class, rte);
    assertEquals("Emitter already completed", rte.getMessage());
    assertInstanceOf(IllegalStateException.class, rte.getCause());
  }

  @Test
  @DisplayName("sendHeartbeatSafely - Broken pipe 등 IOException 시 RuntimeException으로 래핑한다")
  void whenBrokenPipe_thenRuntimeExceptionWithConnectionBroken() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new IOException("Connection broken"))
        .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

    Method m = sendHeartbeatMethod();
    InvocationTargetException ite = assertThrows(
        InvocationTargetException.class,
        () -> m.invoke(emitterService, emitter, "em2")
    );

    Throwable rte = ite.getTargetException();
    assertInstanceOf(RuntimeException.class, rte);
    assertEquals("Connection broken", rte.getMessage());
    assertInstanceOf(IOException.class, rte.getCause());
  }

  @Test
  @DisplayName("sendHeartbeatSafely - 기타 Exception 시 RuntimeException으로 래핑한다")
  void whenOtherException_thenRuntimeExceptionWithHeartbeatFailed() throws Exception {
    SseEmitter emitter = mock(SseEmitter.class);
    doThrow(new RuntimeException("Heartbeat failed"))
        .when(emitter).send(any(SseEmitter.SseEventBuilder.class));

    Method m = sendHeartbeatMethod();
    InvocationTargetException ite = assertThrows(
        InvocationTargetException.class,
        () -> m.invoke(emitterService, emitter, "em3")
    );

    Throwable rte = ite.getTargetException();
    assertInstanceOf(RuntimeException.class, rte);
    assertEquals("Heartbeat failed", rte.getMessage());
    assertInstanceOf(RuntimeException.class, rte.getCause());
  }
}
