package team03.mopl.domain.notification;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import team03.mopl.domain.follow.dto.FollowResponse;
import team03.mopl.domain.follow.service.FollowService;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.events.FollowingPostedPlaylistEvent;
import team03.mopl.domain.notification.events.PlaylistSubscribedEvent;
import team03.mopl.domain.notification.events.PlaylistUpdatedEvent;
import team03.mopl.domain.notification.repository.NotificationRepository;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.subscription.Subscription;
import team03.mopl.domain.subscription.SubscriptionRepository;
import team03.mopl.domain.notification.service.EmitterService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

  private final SubscriptionRepository subscriptionRepository;
  private final NotificationRepository notificationRepository;
  private final EmitterService emitterService;
  private final NotificationService notificationService;
  private final FollowService followService;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePlaylistUpdated(PlaylistUpdatedEvent event) {
    UUID playlistId = event.playlistId();
    List<Subscription> subs = subscriptionRepository.findByPlaylistId(playlistId);
    if (subs.isEmpty()) {
      log.debug("구독자가 없어 알림 전송 없음: playlistId={}", playlistId);
      return;
    }

    String content = "플레이리스트가 업데이트되었습니다: " + event.playlistTitle();
    subs.forEach(s -> notificationService.sendNotification(new NotificationDto(s.getUser().getId(), NotificationType.PLAYLIST_SUBSCRIBED, content)));

    log.info("PlaylistUpdatedEvent 처리 완료: playlistId={}, 알림 수={}", playlistId, subs.size());
  }


  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onPlaylistSubscribed(PlaylistSubscribedEvent event) {
    // 알림 내용은 필요에 따라 가공
    String content = "당신의 " + event.title() + " 플레이리스트에 새로운 구독자가 등록되었습니다.";
    notificationService.sendNotification(new NotificationDto(event.ownerId(), NotificationType.PLAYLIST_SUBSCRIBED, content));
    log.info("PlaylistSubscribedEvent 처리 완료: playlistId={}, ownerId={}, subscriberId={}", event.playlistId(), event.ownerId(), event.subscriberId());
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onFollowingPostedPlaylist(FollowingPostedPlaylistEvent event) {
    UUID creatorId = event.creatorId();
    UUID playlistId = event.playlistId();
    String playlistName = event.playlistName();

    //새로 등록된 플리 -> 나를 팔로우한 사용자에게 알림 전송
    if (!event.isPublic()) {
      log.debug("비공개 플레이리스트 생성: 알림 전송 생략 playlistId={}", playlistId);
      return;
    }

    //나를 팔로우하고 있는 사람들의 목록
    List<FollowResponse> followings = followService.getFollowers(creatorId);

    //목록이 없다면 반환
    if (followings.isEmpty()) {
      log.debug("구독자가 없어 알림 전송 없음: creatorId={}", event.creatorId());
      return;
    }

    String content = "팔로우 중인 사용자(" + event.creatorName() + ")가 새로운 플레이리스트를 만들었습니다: " + playlistName;
    for (FollowResponse f : followings) {
      UUID followingId = f.id();
      notificationService.sendNotification(new NotificationDto(followingId, NotificationType.FOLLOWING_POSTED_PLAYLIST, content));
    }

    log.info("onFollowingPostedPlaylist 처리 완료: creatorId={}, playlistId={}, followerCount={}", event.creatorId(), event.playlistId(),
        followings.size());

  }
}
