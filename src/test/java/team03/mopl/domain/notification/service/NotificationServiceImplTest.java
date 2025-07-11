package team03.mopl.domain.notification.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team03.mopl.common.exception.notification.NotificationNotFoundException;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.dto.NotificationPagingDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.repository.NotificationRepository;
import team03.mopl.domain.notification.repository.NotificationRepositoryCustom;
import team03.mopl.domain.user.UserService;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

  @Mock
  private NotificationRepository notificationRepository;
  @Mock
  private NotificationRepositoryCustom notificationRepositoryCustom;
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
  @DisplayName("getNotifications - 알림 목록 조회 (커서 없이)")
  void getNotifications_noCursor() {
    // given
    UUID receiverId = UUID.randomUUID();
    Notification n1 = new Notification(receiverId, NotificationType.FOLLOWED, "팔로우 알림");
    Notification n2 = new Notification(receiverId, NotificationType.DM_RECEIVED, "DM 알림");

    // 커서 없이 20개 요청, 반환은 2개 (hasNext = false)
    when(notificationRepository.count()).thenReturn(2L);
    when(notificationRepositoryCustom.findByCursor(receiverId, 21, null, null)) // size + 1
        .thenReturn(List.of(n1, n2));

    NotificationPagingDto dto = new NotificationPagingDto(null, 20);

    // when
    var result = notificationService.getNotifications(dto, receiverId);

    // then
    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(result.nextCursor()).isNull();
    assertThat(result.data().get(0).getContent()).isEqualTo("팔로우 알림");
    assertThat(result.data().get(1).getContent()).isEqualTo("DM 알림");
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

  @Test
  @DisplayName("deleteNotificationByUserId - 인증된 사용자가 읽은 알림 삭제 요청 시 삭제됨")
  void deleteNotificationByUserId() {
    // given
    UUID authenticatedUserId = receiverId;

    // when
    notificationService.deleteNotificationByUserId(authenticatedUserId);

    // then
    then(notificationRepository).should().deleteByReceiverIdAndIsRead(receiverId, true);
  }
  @Test
  @DisplayName("deleteNotification - 존재하는 알림 ID로 삭제 요청 시 정상 삭제됨")
  void deleteNotification_success() {
    // given
    UUID notificationId = UUID.randomUUID();
    Notification notification = new Notification(receiverId, NotificationType.DM_RECEIVED, "테스트 알림");
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    // when
    notificationService.deleteNotification(notificationId);

    // then
    then(notificationRepository).should().findById(notificationId);
    then(notificationRepository).should().deleteById(notificationId);
  }

  @Test
  @DisplayName("deleteNotification - 존재하지 않는 알림 ID로 삭제 요청 시 예외 발생")
  void deleteNotification_notFound() {
    // given
    UUID notificationId = UUID.randomUUID();
    given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> notificationService.deleteNotification(notificationId))
        .isInstanceOf(NotificationNotFoundException.class);

    then(notificationRepository).should().findById(notificationId);
    then(notificationRepository).shouldHaveNoMoreInteractions(); // deleteById가 호출되지 않았는지 확인
  }


}
