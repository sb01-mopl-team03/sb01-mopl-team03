package team03.mopl.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import team03.mopl.domain.follow.dto.FollowResponse;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.events.FollowingPostedPlaylistEvent;
import team03.mopl.domain.notification.events.PlaylistSubscribedEvent;
import team03.mopl.domain.notification.events.PlaylistUpdatedEvent;
import team03.mopl.domain.notification.repository.NotificationRepository;
import team03.mopl.domain.notification.service.EmitterService;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.subscription.Subscription;
import team03.mopl.domain.subscription.SubscriptionRepository;
import team03.mopl.domain.user.User;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

  @Mock SubscriptionRepository subscriptionRepository;
  @Mock NotificationRepository notificationRepository;
  @Mock EmitterService emitterService;
  @Mock NotificationService notificationService;
  @Mock FollowService followService;

  @InjectMocks NotificationEventListener listener;

  @DisplayName("플레이리스트 업데이트 시 구독자에게 알림 전송")
  @Test
  void handlePlaylistUpdated_withSubscribers_shouldSendNotifications() {
    // given
    UUID playlistId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    UUID userId1 = UUID.randomUUID();
    UUID userId2 = UUID.randomUUID();

    PlaylistUpdatedEvent event = new PlaylistUpdatedEvent(playlistId, ownerId, "업데이트된 플리제목");

    // Mock user and subscription
    User user1 = mock(User.class);
    User user2 = mock(User.class);
    when(user1.getId()).thenReturn(userId1);
    when(user2.getId()).thenReturn(userId2);

    Subscription sub1 = mock(Subscription.class);
    Subscription sub2 = mock(Subscription.class);
    when(sub1.getUser()).thenReturn(user1);
    when(sub2.getUser()).thenReturn(user2);

    List<Subscription> subs = List.of(sub1, sub2);
    when(subscriptionRepository.findByPlaylistId(playlistId)).thenReturn(subs);

    // when
    listener.handlePlaylistUpdated(event);

    // then
    verify(notificationService, times(2)).sendNotification(argThat(dto ->
        dto.getNotificationType() == NotificationType.PLAYLIST_SUBSCRIBED &&
            dto.getContent().contains("업데이트되었습니다") &&
            (dto.getReceiverId().equals(userId1) || dto.getReceiverId().equals(userId2))
    ));
  }

  @DisplayName("플레이리스트가 구독되면 플레이리스트 소유자에게 알림을 보낸다")
  @Test
  void onPlaylistSubscribed_sendNotificationToOwner() {
    // given
    UUID playlistId   = UUID.randomUUID();
    UUID ownerId      = UUID.randomUUID();
    UUID subscriberId = UUID.randomUUID();
    String title = "title";
    PlaylistSubscribedEvent event =
        new PlaylistSubscribedEvent(playlistId, ownerId, title, subscriberId);

    // when
    listener.onPlaylistSubscribed(event);

    // then
    ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
    verify(notificationService, times(1)).sendNotification(captor.capture());

    NotificationDto dto = captor.getValue();
    assertThat(dto.getReceiverId()).isEqualTo(ownerId);
    assertThat(dto.getNotificationType()).isEqualTo(NotificationType.PLAYLIST_SUBSCRIBED);
    assertThat(dto.getContent()).contains("플레이리스트에 새로운 구독자가 등록되었습니다."); // 메시지 검증

    // 다른 상호작용 없는지
    verifyNoMoreInteractions(notificationService);
  }

  @DisplayName("공개 플리 생성 시 팔로워 전원에게 알림 전송")
  @Test
  void onFollowingPostedPlaylist_public_playlist_sends_notifications() {
    // given
    UUID creatorId = UUID.randomUUID();
    UUID playlistId = UUID.randomUUID();
    String playlistName = "새 플리!";
    String creatorName = "creator";
    FollowingPostedPlaylistEvent event =
        new FollowingPostedPlaylistEvent(creatorId, creatorName, playlistId, playlistName, true);

    FollowResponse follower1 = new FollowResponse(UUID.randomUUID(), "user1", "img1", null, null);
    FollowResponse follower2 = new FollowResponse(UUID.randomUUID(), "user2", "img2", null, null);
    when(followService.getFollowers(creatorId)).thenReturn(List.of(follower1, follower2));

    // when
    listener.onFollowingPostedPlaylist(event);

    // then
    ArgumentCaptor<NotificationDto> captor = ArgumentCaptor.forClass(NotificationDto.class);
    //---------------------------------follower1, follower2
    verify(notificationService, times(2)).sendNotification(captor.capture());

    List<NotificationDto> dtos = captor.getAllValues();
    assertThat(dtos).hasSize(2);
    assertThat(dtos).extracting(NotificationDto::getReceiverId).containsExactlyInAnyOrder(follower1.id(), follower2.id());
    assertThat(dtos).allSatisfy(dto -> {
      assertThat(dto.getNotificationType()).isEqualTo(NotificationType.FOLLOWING_POSTED_PLAYLIST);
      assertThat(dto.getContent()).contains(playlistName);
      assertThat(dto.getContent()).contains(creatorName);
    });

    verifyNoMoreInteractions(notificationService);
  }

  @DisplayName("비공개 플리면 아무에게도 안 보낸다")
  @Test
  void onFollowingPostedPlaylist_private_playlist_no_notification() {
    // given
    UUID creatorId = UUID.randomUUID();
    String creatorName = "creator";
    FollowingPostedPlaylistEvent event =
        new FollowingPostedPlaylistEvent(creatorId, creatorName, UUID.randomUUID(), "secret", false);

    // when
    listener.onFollowingPostedPlaylist(event);

    // then
    verifyNoInteractions(notificationService);
    verifyNoInteractions(followService); // 비공개면 팔로워 조회 자체를 안 함
  }

  @DisplayName("팔로워가 없으면 알림 전송 안 함")
  @Test
  void onFollowingPostedPlaylist_no_followers() {
    // given
    UUID creatorId = UUID.randomUUID();
    String creatorName = "creator";
    FollowingPostedPlaylistEvent event =
        new FollowingPostedPlaylistEvent(creatorId, creatorName, UUID.randomUUID(), "empty world", true);

    when(followService.getFollowers(creatorId)).thenReturn(List.of());

    // when
    listener.onFollowingPostedPlaylist(event);

    // then
    verify(followService).getFollowers(creatorId);
    verifyNoInteractions(notificationService);
  }
}

