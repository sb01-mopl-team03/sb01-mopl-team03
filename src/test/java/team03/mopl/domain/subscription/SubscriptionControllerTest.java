package team03.mopl.domain.subscription;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import team03.mopl.common.exception.playlist.PlaylistNotFoundException;
import team03.mopl.common.exception.subscription.AlreadySubscribedException;
import team03.mopl.common.exception.subscription.SelfSubscriptionNotAllowedException;
import team03.mopl.common.exception.subscription.SubscriptionDeleteDeniedException;
import team03.mopl.common.exception.subscription.SubscriptionNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.subscription.dto.SubscribeRequest;
import team03.mopl.domain.subscription.dto.SubscriptionDto;
import team03.mopl.domain.subscription.service.SubscriptionService;
import team03.mopl.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("구독 컨트롤러 테스트")
class SubscriptionControllerTest {

  @Mock
  private SubscriptionService subscriptionService;

  @Mock
  private CustomUserDetails userDetails;

  @InjectMocks
  private SubscriptionController subscriptionController;

  @Nested
  @DisplayName("구독 생성 요청")
  class Subscribe {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      UUID playlistId = UUID.randomUUID();
      UUID subscriptionId = UUID.randomUUID();

      SubscribeRequest request = new SubscribeRequest(userId, playlistId);
      SubscriptionDto mockResponse = new SubscriptionDto(subscriptionId, userId, playlistId);

      when(subscriptionService.subscribe(userId, playlistId)).thenReturn(mockResponse);

      // when
      ResponseEntity<SubscriptionDto> response = subscriptionController.subscribe(request);

      // then
      assertNotNull(response.getBody());
      assertEquals(201, response.getStatusCodeValue()); // CREATED
      assertEquals(mockResponse.subscriptionId(), response.getBody().subscriptionId());
      assertEquals(mockResponse.userId(), response.getBody().userId());
      assertEquals(mockResponse.playlistId(), response.getBody().playlistId());
      verify(subscriptionService).subscribe(userId, playlistId);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      UUID playlistId = UUID.randomUUID();

      SubscribeRequest request = new SubscribeRequest(userId, playlistId);

      when(subscriptionService.subscribe(userId, playlistId)).thenThrow(new UserNotFoundException());

      // when & then
      assertThrows(UserNotFoundException.class, () -> subscriptionController.subscribe(request));
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      UUID playlistId = UUID.randomUUID();

      SubscribeRequest request = new SubscribeRequest(userId, playlistId);

      when(subscriptionService.subscribe(userId, playlistId)).thenThrow(new PlaylistNotFoundException());

      // when & then
      assertThrows(PlaylistNotFoundException.class, () -> subscriptionController.subscribe(request));
    }

    @Test
    @DisplayName("자신의 플레이리스트 구독 시도")
    void failsWhenSelfSubscription() {
      // given
      UUID userId = UUID.randomUUID();
      UUID playlistId = UUID.randomUUID();

      SubscribeRequest request = new SubscribeRequest(userId, playlistId);

      when(subscriptionService.subscribe(userId, playlistId)).thenThrow(new SelfSubscriptionNotAllowedException());

      // when & then
      assertThrows(SelfSubscriptionNotAllowedException.class, () -> subscriptionController.subscribe(request));
    }

    @Test
    @DisplayName("이미 구독 중인 플레이리스트")
    void failsWhenAlreadySubscribed() {
      // given
      UUID userId = UUID.randomUUID();
      UUID playlistId = UUID.randomUUID();

      SubscribeRequest request = new SubscribeRequest(userId, playlistId);

      when(subscriptionService.subscribe(userId, playlistId)).thenThrow(new AlreadySubscribedException());

      // when & then
      assertThrows(AlreadySubscribedException.class, () -> subscriptionController.subscribe(request));
    }
  }

  @Nested
  @DisplayName("구독 취소 요청")
  class Unsubscribe {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID subscriptionId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);

      // when
      ResponseEntity<Void> response = subscriptionController.unsubscribe(subscriptionId, userDetails);

      // then
      assertEquals(204, response.getStatusCodeValue()); // NO_CONTENT
      assertNull(response.getBody());
      verify(subscriptionService).unsubscribe(subscriptionId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 구독")
    void failsWhenSubscriptionNotFound() {
      // given
      UUID subscriptionId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new SubscriptionNotFoundException()).when(subscriptionService).unsubscribe(subscriptionId, userId);

      // when & then
      assertThrows(SubscriptionNotFoundException.class,
          () -> subscriptionController.unsubscribe(subscriptionId, userDetails));
    }

    @Test
    @DisplayName("다른 사용자의 구독 취소 시도")
    void failsWhenSubscriptionDeleteDenied() {
      // given
      UUID subscriptionId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new SubscriptionDeleteDeniedException()).when(subscriptionService).unsubscribe(subscriptionId, userId);

      // when & then
      assertThrows(SubscriptionDeleteDeniedException.class,
          () -> subscriptionController.unsubscribe(subscriptionId, userDetails));
    }
  }
}
