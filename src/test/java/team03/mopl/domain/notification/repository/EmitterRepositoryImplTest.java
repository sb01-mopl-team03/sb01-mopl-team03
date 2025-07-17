package team03.mopl.domain.notification.repository;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

class EmitterRepositoryImplTest {

  private EmitterRepository emitterRepository = new EmitterRepositoryImpl();
  private EmitterCacheRepository cacheRepository = new EmitterCacheRepositoryImpl();
  private Long DEFAULT_TIMEOUT = 60L * 1000L * 60L;
  private UUID userId;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
  }

  private String makeEmitterId(UUID userId) {
    return userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID();
  }

  private String makeNotificationCacheId(String emitterId, UUID notificationId, UUID userId) {
    return notificationId + "_" + emitterId + "_" + userId;
  }
  
  @Test
  @DisplayName("새로운 Emitter를 추가한다.")
  void save() {
    SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);
    String emitterId = makeEmitterId(userId);

    Assertions.assertDoesNotThrow(() -> emitterRepository.saveEmitter(emitterId, sseEmitter));
  }

  @Test
  @DisplayName("수신한 알림를 캐시에 저장한다.")
  void saveNotificationCache() {
    String emitterId = makeEmitterId(userId);
    Notification notification = new Notification(userId, NotificationType.FOLLOWED, "알림 전송");
    //추가 고민 필요
    String notificationCacheId = makeNotificationCacheId(emitterId, UUID.randomUUID(), userId);
    Assertions.assertDoesNotThrow(() -> cacheRepository.saveNotificationCache(notificationCacheId, notification));
  }

  @Test
  @DisplayName("어떤 회원이 접속한 모든 Emitter를 찾는다")
  void findAllEmitterStartWithByMemberId() throws InterruptedException {
    //given
    String emitterId1 = userId + "_" + System.currentTimeMillis();
    emitterRepository.saveEmitter(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId2 = userId + "_" + System.currentTimeMillis();
    emitterRepository.saveEmitter(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId3 = userId + "_" + System.currentTimeMillis();
    emitterRepository.saveEmitter(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));


    //when
    Map<String, SseEmitter> ActualResult = emitterRepository.findAllEmittersByUserIdPrefix(String.valueOf(userId));

    //then
    Assertions.assertEquals(3, ActualResult.size());
  }

  @Test
  @DisplayName("어떤 회원에게 수신된 이벤트를 캐시에서 모두 찾는다.")
  void findAllEventCacheStartWithByMemberId() throws InterruptedException {
    UUID notificationId = UUID.randomUUID();
    String notificationCacheId1 = makeNotificationCacheId(makeEmitterId(notificationId), UUID.randomUUID(), userId);
    Notification notification1 = new Notification(userId, NotificationType.FOLLOWED, "팔로우 했습니다.");
    cacheRepository.saveNotificationCache(notificationCacheId1, notification1);

    Thread.sleep(100);
    String notificationCacheId2 = makeNotificationCacheId(makeEmitterId(notificationId), UUID.randomUUID(), userId);
    Notification notification2 = new Notification(userId, NotificationType.DM_RECEIVED, "DM이 도착했습니다..");
    cacheRepository.saveNotificationCache(notificationCacheId2, notification2);

    Thread.sleep(100);
    String notificationCacheId3 = makeNotificationCacheId(makeEmitterId(notificationId), UUID.randomUUID(), userId);
    Notification notification3 = new Notification(userId, NotificationType.UNFOLLOWED, "언팔로우 했습니다.");
    cacheRepository.saveNotificationCache(notificationCacheId3, notification3);

    //when
    Map<String, Notification> ActualResult = cacheRepository.findAllNotificationCachesByUserIdPrefix(userId);

    //then
    Assertions.assertEquals(3, ActualResult.size());
  }

  @Test
  @DisplayName("ID를 통해 Emitter를 Repository에서 제거한다.")
  public void deleteEmitterById() throws Exception {
    //given
    String emitterId =  userId + "_" + System.currentTimeMillis();
    SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

    //when
    emitterRepository.saveEmitter(emitterId, sseEmitter);
    emitterRepository.deleteEmitterById(emitterId);

    //then
    Assertions.assertEquals(0, emitterRepository.findAllEmittersByUserIdPrefix(emitterId).size());
  }

  @Test
  @DisplayName("저장된 모든 Emitter를 제거한다.")
  public void deleteAllEmitterStartWithId() throws Exception {
    //given
    String emitterId1 = userId + "_" + System.currentTimeMillis();
    emitterRepository.saveEmitter(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

    Thread.sleep(100);
    String emitterId2 = userId + "_" + System.currentTimeMillis();
    emitterRepository.saveEmitter(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

    //when
    emitterRepository.deleteAllEmittersByUserIdPrefix(String.valueOf(userId));

    //then
    Assertions.assertEquals(0, emitterRepository.findAllEmittersByUserIdPrefix(String.valueOf(userId)).size());
  }

  @Test
  @DisplayName("이벤트 캐시를 삭제하면 해당 멤버의 캐시가 모두 비워진다.")
  public void deleteAllNotificationCacheStartWithId() throws Exception {
    //given
    UUID notificationId = UUID.randomUUID();
    String notificationCacheId1 = makeNotificationCacheId(makeEmitterId(notificationId), UUID.randomUUID(), userId);
    Notification notification1 = new Notification(userId, NotificationType.FOLLOWED, "팔로우 했습니다.");
    cacheRepository.saveNotificationCache(notificationCacheId1, notification1);

    Thread.sleep(100);
    String notificationCacheId2 = makeNotificationCacheId(makeEmitterId(notificationId), UUID.randomUUID(), userId);
    Notification notification2 = new Notification(userId, NotificationType.DM_RECEIVED, "DM이 도착했습니다..");
    cacheRepository.saveNotificationCache(notificationCacheId2, notification2);

    //when
    cacheRepository.deleteAllNotificationCachesByUserIdPrefix(String.valueOf(userId));

    //then
    Assertions.assertEquals(0, cacheRepository.findAllNotificationCachesByNotificationIdPrefix(notificationId).size());
  }
}