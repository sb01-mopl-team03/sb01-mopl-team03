/*
package team03.mopl.domain.notification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.repository.EmitterRepository;

@ExtendWith(MockitoExtension.class)
class EmitterServiceTest {

  @Mock
  private EmitterRepository emitterRepository;

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
*/
