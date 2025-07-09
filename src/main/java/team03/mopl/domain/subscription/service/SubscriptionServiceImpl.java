package team03.mopl.domain.subscription.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.subscription.AlreadySubscribedException;
import team03.mopl.common.exception.subscription.NotSubscribedException;
import team03.mopl.common.exception.subscription.SelfSubscriptionNotAllowedException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.playlist.Playlist;
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

  @Override
  public SubscriptionDto subscribe(UUID userId, UUID playlistId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException());

    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(() -> new PlaylistNotFoundException());

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

    return SubscriptionDto.from(savedSubscription);
  }

  // TODO: 이것도 구독한 사용자가 취소요청하는지 확인해야하나? (컨트롤러에서 @AUthentication~~ 사용)
  @Override
  public void unsubscribe(UUID userId, UUID playlistId) {

    if (!subscriptionRepository.existsByUserIdAndPlaylistId(userId, playlistId)) {
      throw new NotSubscribedException();
    }

    subscriptionRepository.deleteByUserIdAndPlaylistId(userId, playlistId);
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
