package team03.mopl.domain.notification.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.repository.NotificationRepository;
import team03.mopl.domain.user.UserService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private EmitterService emitterService;
  @Mock
  UserService userService;

  @InjectMocks
  private NotificationServiceImpl notificationService;

  private UUID receiverId;

  @BeforeEach
  void setUp() {
    receiverId = UUID.randomUUID();
  }

  @Test
  @DisplayName("sendNotification - 알림 전송")
  void sendNotification() {
    // given
    NotificationType type = NotificationType.NEW_DM_ROOM;
    String content = "새로운 DM이 도착했습니다.";

    Notification saved = new Notification(receiverId, type, content);
    given(notificationRepository.save(any(Notification.class))).willReturn(saved);

    // when
    NotificationDto notificationDto = new NotificationDto(receiverId, type, content);
    notificationService.sendNotification(notificationDto);

    // then
    then(notificationRepository).should().save(any(Notification.class));
    then(emitterService).should().sendNotificationToMember(receiverId, saved);
  }

  @Test
  @DisplayName("getNotifications - 알림 목록 조회")
  void getNotifications() {
    // given
    UUID receiverId = UUID.randomUUID();
    Notification n1 = new Notification(receiverId, NotificationType.FOLLOWED, "팔로우 알림");
    Notification n2 = new Notification(receiverId, NotificationType.DM_RECEIVED, "DM 알림");

    when(notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId))
        .thenReturn(List.of(n1, n2));

    // when
    var result = notificationService.getNotifications(receiverId);

    // then
    assertThat(result).hasSize(2);
    assertThat(result.get(0).getContent()).isEqualTo("팔로우 알림");
    assertThat(result.get(1).getContent()).isEqualTo("DM 알림");
  }

  @Test
  @DisplayName("markAllAsRead - 모든 알림 읽음 처리")
  void markAllAsRead() {
    // given
    Notification n1 = new Notification(receiverId, NotificationType.FOLLOWED, "팔로우 알림");
    Notification n2 = new Notification(receiverId, NotificationType.DM_RECEIVED, "DM 알림");

    // isRead false 초기값
    given(notificationRepository.findByReceiverIdAndIsRead(receiverId, false)).willReturn(List.of(n1, n2));

    // when
    notificationService.markAllAsRead(receiverId);

    // then
    assertThat(n1.isRead()).isTrue();
    assertThat(n2.isRead()).isTrue();
    then(notificationRepository).should().saveAll(anyList());
  }
}
