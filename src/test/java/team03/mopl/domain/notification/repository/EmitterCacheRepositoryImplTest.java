package team03.mopl.domain.notification.repository;

import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

class EmitterCacheRepositoryImplTest {

  private EmitterCacheRepository cacheRepository = new EmitterCacheRepositoryImpl();
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
  @DisplayName("NotificationCache 삭제 By Id")
  public void deleteNotificationCacheById() throws InterruptedException {
    //given
    UUID notificationId = UUID.randomUUID();
    String notificationCacheId1 = makeNotificationCacheId(makeEmitterId(notificationId), UUID.randomUUID(), userId);
    System.out.println("notificationCacheId1 = " + notificationCacheId1);
    Notification notification1 = new Notification(userId, NotificationType.FOLLOWED, "팔로우 했습니다.");
    cacheRepository.saveNotificationCache(notificationCacheId1, notification1);

    Thread.sleep(100);
    String notificationCacheId2 = makeNotificationCacheId(makeEmitterId(notificationId), UUID.randomUUID(), userId);
    System.out.println("notificationCacheId2 = " + notificationCacheId2);
    Notification notification2 = new Notification(userId, NotificationType.DM_RECEIVED, "DM이 도착했습니다..");
    cacheRepository.saveNotificationCache(notificationCacheId2, notification2);

    //when
    cacheRepository.deleteNotificationCacheById(notificationCacheId1);

    //then
    //Assertions.assertEquals(1, cacheRepository.findAllNotificationCachesByNotificationIdPrefix(notificationId).size());
    Assertions.assertEquals(1, cacheRepository.findAllNotificationCachesByUserIdPrefix(userId).size());
  }
  @Test
  @DisplayName("NotificationCache 삭제 By 알림ID")
  public void deleteNotificationCachesByNotificationIdPrefix() throws InterruptedException {
    //given
    UUID notificationId = UUID.randomUUID();
    String notificationCacheId1 = makeNotificationCacheId(makeEmitterId(notificationId), notificationId, userId);
    System.out.println("notificationCacheId1 = " + notificationCacheId1);
    Notification notification1 = new Notification(userId, NotificationType.FOLLOWED, "팔로우 했습니다.");
    cacheRepository.saveNotificationCache(notificationCacheId1, notification1);

    Thread.sleep(100);
    String notificationCacheId2 = makeNotificationCacheId(makeEmitterId(notificationId), notificationId, userId);
    System.out.println("notificationCacheId2 = " + notificationCacheId2);
    Notification notification2 = new Notification(userId, NotificationType.DM_RECEIVED, "DM이 도착했습니다..");
    cacheRepository.saveNotificationCache(notificationCacheId2, notification2);

    //when
    cacheRepository.deleteNotificationCachesByNotificationIdPrefix(notificationId);

    //then
    Assertions.assertEquals(0, cacheRepository.findAllNotificationCachesByUserIdPrefix(userId).size());
  }
}