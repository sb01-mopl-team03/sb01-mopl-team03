package team03.mopl.domain.subscription.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.playlist.PlaylistNotFoundException;
import team03.mopl.common.exception.subscription.AlreadySubscribedException;
import team03.mopl.common.exception.subscription.SelfSubscriptionNotAllowedException;
import team03.mopl.common.exception.subscription.SubscriptionDeleteDeniedException;
import team03.mopl.common.exception.subscription.SubscriptionNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.notification.events.PlaylistSubscribedEvent;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.repository.PlaylistRepository;
import team03.mopl.domain.subscription.Subscription;
import team03.mopl.domain.subscription.dto.SubscriptionDto;
import team03.mopl.domain.subscription.SubscriptionRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final UserRepository userRepository;
  private final PlaylistRepository playlistRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public SubscriptionDto subscribe(UUID userId, UUID playlistId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(PlaylistNotFoundException::new);

    if (playlist.getUser().getId().equals(userId)) {
      throw new SelfSubscriptionNotAllowedException();
    }

    // 이미 구독 중인지 확인
    if (subscriptionRepository.existsByUserIdAndPlaylistId(userId, playlistId)) {
      throw new AlreadySubscribedException();
    }

    Subscription subscription = Subscription.builder()
        .user(user)
        .playlist(playlist)
        .build();
    Subscription savedSubscription = subscriptionRepository.save(subscription);

    eventPublisher.publishEvent(new PlaylistSubscribedEvent(
        playlist.getId(),
        playlist.getUser().getId(),
        user.getId()
    ));

    return SubscriptionDto.from(savedSubscription);
  }

  @Override
  @Transactional
  public void unsubscribe(UUID subscriptionId, UUID userId) {

    Subscription subscription = subscriptionRepository.findById(subscriptionId).orElseThrow(
        SubscriptionNotFoundException::new);

    if (!subscription.getUser().getId().equals(userId)) {
      log.warn("구독자만 구독 취소할 수 있습니다. 구독자 ID: ", userId);
      throw new SubscriptionDeleteDeniedException();
    }

    subscriptionRepository.deleteById(subscriptionId);
  }

  @Override
  @Transactional(readOnly = true)
  public List<SubscriptionDto> getSubscriptions(UUID userId) {
    List<Subscription> subscriptions = subscriptionRepository.findByUserId(userId);
    return subscriptions.stream()
        .map(SubscriptionDto::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<SubscriptionDto> getSubscribers(UUID playlistId) {
    List<Subscription> subscriptions = subscriptionRepository.findByPlaylistId(playlistId);
    return subscriptions.stream()
        .map(SubscriptionDto::from)
        .toList();
  }
}
