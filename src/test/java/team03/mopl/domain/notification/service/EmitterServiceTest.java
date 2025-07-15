//package team03.mopl.domain.notification.service;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.BDDMockito.given;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//import team03.mopl.domain.notification.entity.Notification;
//import team03.mopl.domain.notification.entity.NotificationType;
//import team03.mopl.domain.notification.repository.EmitterRepository;
//
//@ExtendWith(MockitoExtension.class)
//class EmitterServiceTest {
//
//  @Mock
//  private EmitterRepository emitterRepository;
//  @Mock
//  private NotificationService notificationService;
//
//  @InjectMocks
//  private EmitterService emitterService;
//
//  @Test
//  @DisplayName("subscribe 메서드가 emitter를 저장한다")
//  void subscribe_shouldSaveEmitter() {
//    // given
//    UUID userId = UUID.randomUUID();
//    String lastNotificationId = null;
//
//    // when
//    var emitter = emitterService.subscribe(userId, lastNotificationId);
//
//    // then
//    verify(emitterRepository, times(1)).saveEmitter(anyString(), eq(emitter));
//  }
//
//  @Test
//  void handleEmitterTermination_shouldDeleteEmitter() {
//    emitterService.handleEmitterTermination("emitter-id-123", "onCompletion");
//    verify(emitterRepository).deleteEmitterById("emitter-id-123");
//  }
//
//  @Test
//  void handleEmitterError_shouldDeleteEmitterAndLog() {
//    emitterService.handleEmitterError("emitter-id-456", new RuntimeException("fail"));
//    verify(emitterRepository).deleteEmitterById("emitter-id-456");
//  }
//
//
//  @Test
//  @DisplayName("sendNotificationToMember가 emitter에 데이터를 전송하고 캐시에 저장한다")
//  void sendNotificationToMember_shouldSendAndCache() throws Exception {
//    // given
//    UUID userId = UUID.randomUUID();
//    UUID notificationId = UUID.randomUUID();
//    Notification notification = new Notification(
//        userId,
//        NotificationType.DM_RECEIVED,
//        "hello"
//    );
//    ReflectionTestUtils.setField(notification, "id", notificationId);
//
//    SseEmitter mockEmitter = mock(SseEmitter.class);
//
//    // emitterId를 실제 코드랑 맞게 생성
//    String emitterId = userId + "_emitterId";
//    Map<String, SseEmitter> emitters = Map.of(
//        emitterId, mockEmitter
//    );
//
//    given(emitterRepository.findAllEmittersByUserIdPrefix(anyString()))
//        .willReturn(emitters);
//
//    // when
//    emitterService.sendNotificationToMember(userId, notification);
//
//    // then
//    String expectedCacheId = notificationId + "_" + emitterId + "_" + userId;
//    verify(mockEmitter, times(1)).send(any(SseEmitter.SseEventBuilder.class));
//    verify(emitterRepository, times(1)).saveNotificationCache(
//        eq(expectedCacheId),
//        eq(notification)
//    );
//  }
//
//  @Test
//  @DisplayName("여러 emitter에 알림을 전송하고 각각 캐시에 저장한다")
//  void sendNotificationToMultipleEmitters_shouldSendAndCacheAll() throws Exception {
//    // given
//    UUID userId = UUID.randomUUID();
//    UUID notificationId = UUID.randomUUID();
//
//    Notification notification = new Notification(
//        userId,
//        NotificationType.DM_RECEIVED,
//        "hello"
//    );
//    // 테스트를 위해 강제 ID 주입
//    ReflectionTestUtils.setField(notification, "id", notificationId);
//
//    // emitter 2개를 mock
//    SseEmitter emitter1 = mock(SseEmitter.class);
//    SseEmitter emitter2 = mock(SseEmitter.class);
//
//    String emitterId1 = userId + "_emitter1";
//    String emitterId2 = userId + "_emitter2";
//
//    Map<String, SseEmitter> emitters = Map.of(
//        emitterId1, emitter1,
//        emitterId2, emitter2
//    );
//
//    given(emitterRepository.findAllEmittersByUserIdPrefix(anyString()))
//        .willReturn(emitters);
//
//    // when
//    emitterService.sendNotificationToMember(userId, notification);
//
//    // then
//    // 각각의 emitter에 send()가 호출되는지
//    verify(emitter1, times(1)).send(any(SseEmitter.SseEventBuilder.class));
//    verify(emitter2, times(1)).send(any(SseEmitter.SseEventBuilder.class));
//
//    // 각각의 캐시가 올바르게 저장되는지
//    String expectedCacheId1 = notificationId + "_" + emitterId1 + "_" + userId;
//    String expectedCacheId2 = notificationId + "_" + emitterId2 + "_" + userId;
//
//    verify(emitterRepository, times(1))
//        .saveNotificationCache(eq(expectedCacheId1), eq(notification));
//    verify(emitterRepository, times(1))
//        .saveNotificationCache(eq(expectedCacheId2), eq(notification));
//  }
//
//
//  @Test
//  @DisplayName("deleteNotificationCaches는 알림 캐시를 삭제한다")
//  void deleteNotificationCaches_shouldDeleteCaches() {
//    // given
//    UUID notificationId = UUID.randomUUID();
//    Notification notification = new Notification(
//        UUID.randomUUID(),
//        NotificationType.DM_RECEIVED,
//        "bye"
//    );
//    ReflectionTestUtils.setField(notification, "id", notificationId);
//
//    // when
//    emitterService.deleteNotificationCaches(List.of(notification));
//
//    // then
//    verify(emitterRepository, times(1)).deleteNotificationCachesByNotificationIdPrefix(notificationId);
//  }
//}
