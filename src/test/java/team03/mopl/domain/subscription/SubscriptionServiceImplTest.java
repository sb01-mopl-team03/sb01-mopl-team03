package team03.mopl.domain.subscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team03.mopl.common.exception.playlist.PlaylistNotFoundException;
import team03.mopl.common.exception.subscription.AlreadySubscribedException;
import team03.mopl.common.exception.subscription.SelfSubscriptionNotAllowedException;
import team03.mopl.common.exception.subscription.SubscriptionDeleteDeniedException;
import team03.mopl.common.exception.subscription.SubscriptionNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.notification.events.PlaylistSubscribedEvent;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.repository.PlaylistRepository;
import team03.mopl.domain.subscription.dto.SubscriptionDto;
import team03.mopl.domain.subscription.service.SubscriptionServiceImpl;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("구독 서비스 테스트")
class SubscriptionServiceImplTest {

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private PlaylistRepository playlistRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @InjectMocks
  private SubscriptionServiceImpl subscriptionService;

  // 테스트용 구독자 유저
  private UUID subscriberUserId;
  private User subscriberUser;

  // 테스트용 플레이리스트 소유자 유저
  private UUID playlistOwnerUserId;
  private User playlistOwnerUser;

  // 테스트용 플레이리스트
  private UUID playlistId;
  private Playlist playlist;

  // 테스트용 구독
  private UUID subscriptionId;
  private Subscription subscription;

  @BeforeEach
  void setUp() {
    subscriberUserId = UUID.randomUUID();
    subscriberUser = User.builder()
        .id(subscriberUserId)
        .name("구독자")
        .email("subscriber@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    playlistOwnerUserId = UUID.randomUUID();
    playlistOwnerUser = User.builder()
        .id(playlistOwnerUserId)
        .name("플레이리스트소유자")
        .email("owner@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    playlistId = UUID.randomUUID();
    playlist = Playlist.builder()
        .id(playlistId)
        .name("테스트 플레이리스트")
        .user(playlistOwnerUser)
        .createdAt(LocalDateTime.now())
        .build();

    subscriptionId = UUID.randomUUID();
    subscription = Subscription.builder()
        .id(subscriptionId)
        .user(subscriberUser)
        .playlist(playlist)
        .build();
  }

  @Nested
  @DisplayName("구독 생성")
  class Subscribe {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      when(userRepository.findById(subscriberUserId)).thenReturn(Optional.of(subscriberUser));
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
      when(subscriptionRepository.existsByUserIdAndPlaylistId(subscriberUserId, playlistId)).thenReturn(false);
      when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

      // when
      SubscriptionDto result = subscriptionService.subscribe(subscriberUserId, playlistId);

      // then
      assertNotNull(result);
      assertEquals(subscription.getId(), result.subscriptionId());
      assertEquals(subscriberUser.getId(), result.userId());
      assertEquals(playlist.getId(), result.playlistId());

      verify(subscriptionRepository, times(1)).save(any(Subscription.class));
      verify(eventPublisher, times(1)).publishEvent(any(PlaylistSubscribedEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();

      when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> subscriptionService.subscribe(randomUserId, playlistId));

      verify(subscriptionRepository, never()).save(any(Subscription.class));
      verify(eventPublisher, never()).publishEvent(any(PlaylistSubscribedEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();

      when(userRepository.findById(subscriberUserId)).thenReturn(Optional.of(subscriberUser));
      when(playlistRepository.findById(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> subscriptionService.subscribe(subscriberUserId, randomPlaylistId));

      verify(subscriptionRepository, never()).save(any(Subscription.class));
      verify(eventPublisher, never()).publishEvent(any(PlaylistSubscribedEvent.class));
    }

    @Test
    @DisplayName("자신의 플레이리스트 구독 시도")
    void failsWhenSelfSubscription() {
      // given
      when(userRepository.findById(playlistOwnerUserId)).thenReturn(Optional.of(playlistOwnerUser));
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(SelfSubscriptionNotAllowedException.class,
          () -> subscriptionService.subscribe(playlistOwnerUserId, playlistId));

      verify(subscriptionRepository, never()).save(any(Subscription.class));
      verify(eventPublisher, never()).publishEvent(any(PlaylistSubscribedEvent.class));
    }

    @Test
    @DisplayName("이미 구독 중인 플레이리스트")
    void failsWhenAlreadySubscribed() {
      // given
      when(userRepository.findById(subscriberUserId)).thenReturn(Optional.of(subscriberUser));
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
      when(subscriptionRepository.existsByUserIdAndPlaylistId(subscriberUserId, playlistId)).thenReturn(true);

      // when & then
      assertThrows(AlreadySubscribedException.class,
          () -> subscriptionService.subscribe(subscriberUserId, playlistId));

      verify(subscriptionRepository, never()).save(any(Subscription.class));
      verify(eventPublisher, never()).publishEvent(any(PlaylistSubscribedEvent.class));
    }
  }

  @Nested
  @DisplayName("구독 취소")
  class Unsubscribe {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

      // when
      subscriptionService.unsubscribe(subscriptionId, subscriberUserId);

      // then
      verify(subscriptionRepository, times(1)).deleteById(subscriptionId);
    }

    @Test
    @DisplayName("존재하지 않는 구독")
    void failsWhenSubscriptionNotFound() {
      // given
      UUID randomSubscriptionId = UUID.randomUUID();

      when(subscriptionRepository.findById(randomSubscriptionId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(SubscriptionNotFoundException.class,
          () -> subscriptionService.unsubscribe(randomSubscriptionId, subscriberUserId));

      verify(subscriptionRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("다른 사용자의 구독 취소 시도")
    void failsWhenNotSubscriptionOwner() {
      // given
      UUID otherUserId = UUID.randomUUID();

      when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

      // when & then
      assertThrows(SubscriptionDeleteDeniedException.class,
          () -> subscriptionService.unsubscribe(subscriptionId, otherUserId));

      verify(subscriptionRepository, never()).deleteById(any(UUID.class));
    }
  }

  @Nested
  @DisplayName("사용자별 구독 목록 조회")
  class GetSubscriptions {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      Subscription subscription2 = Subscription.builder()
          .id(UUID.randomUUID())
          .user(subscriberUser)
          .playlist(playlist)
          .build();

      List<Subscription> subscriptions = List.of(subscription, subscription2);

      when(subscriptionRepository.findByUserId(subscriberUserId)).thenReturn(subscriptions);

      // when
      List<SubscriptionDto> result = subscriptionService.getSubscriptions(subscriberUserId);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(subscription.getId(), result.get(0).subscriptionId());
      assertEquals(subscription2.getId(), result.get(1).subscriptionId());
    }

    @Test
    @DisplayName("구독이 없는 경우")
    void returnsEmptyListWhenNoSubscriptions() {
      // given
      when(subscriptionRepository.findByUserId(subscriberUserId)).thenReturn(List.of());

      // when
      List<SubscriptionDto> result = subscriptionService.getSubscriptions(subscriberUserId);

      // then
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("플레이리스트별 구독자 목록 조회")
  class GetSubscribers {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      User anotherSubscriber = User.builder()
          .id(UUID.randomUUID())
          .name("다른구독자")
          .email("another@test.com")
          .password("test")
          .role(Role.USER)
          .build();

      Subscription subscription2 = Subscription.builder()
          .id(UUID.randomUUID())
          .user(anotherSubscriber)
          .playlist(playlist)
          .build();

      List<Subscription> subscriptions = List.of(subscription, subscription2);

      when(subscriptionRepository.findByPlaylistId(playlistId)).thenReturn(subscriptions);

      // when
      List<SubscriptionDto> result = subscriptionService.getSubscribers(playlistId);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(subscription.getId(), result.get(0).subscriptionId());
      assertEquals(subscription2.getId(), result.get(1).subscriptionId());
    }

    @Test
    @DisplayName("구독자가 없는 경우")
    void returnsEmptyListWhenNoSubscribers() {
      // given
      when(subscriptionRepository.findByPlaylistId(playlistId)).thenReturn(List.of());

      // when
      List<SubscriptionDto> result = subscriptionService.getSubscribers(playlistId);

      // then
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("이벤트 발행 테스트")
  class EventPublishTest {

    @Test
    @DisplayName("구독 생성 시 PlaylistSubscribedEvent 발행")
    void publishesPlaylistSubscribedEventOnSubscribe() {
      // given
      when(userRepository.findById(subscriberUserId)).thenReturn(Optional.of(subscriberUser));
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
      when(subscriptionRepository.existsByUserIdAndPlaylistId(subscriberUserId, playlistId)).thenReturn(false);
      when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

      // when
      subscriptionService.subscribe(subscriberUserId, playlistId);

      // then
      ArgumentCaptor<PlaylistSubscribedEvent> eventCaptor = ArgumentCaptor.forClass(PlaylistSubscribedEvent.class);
      verify(eventPublisher).publishEvent(eventCaptor.capture());

      PlaylistSubscribedEvent capturedEvent = eventCaptor.getValue();
      assertEquals(playlistId, capturedEvent.playlistId());
      assertEquals(playlistOwnerUserId, capturedEvent.ownerId());
      assertEquals(playlist.getName(), capturedEvent.title());
      assertEquals(subscriberUserId, capturedEvent.subscriberId());
    }
  }
}
