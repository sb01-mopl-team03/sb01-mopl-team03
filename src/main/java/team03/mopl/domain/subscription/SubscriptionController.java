package team03.mopl.domain.subscription;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.domain.subscription.dto.SubscribeRequest;
import team03.mopl.domain.subscription.dto.SubscriptionDto;
import team03.mopl.domain.subscription.service.SubscriptionService;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

  private final SubscriptionService subscriptionService;

  @PostMapping
  public ResponseEntity<SubscriptionDto> subscribe(
      @RequestBody SubscribeRequest request) {
    SubscriptionDto subscription = subscriptionService.subscribe(
        request.userId(), request.playlistId());
    return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
  }

  @DeleteMapping
  public ResponseEntity<Void> unsubscribe(
      @RequestParam UUID userId,
      @RequestParam UUID playlistId) {
    subscriptionService.unsubscribe(userId, playlistId);
    return ResponseEntity.noContent().build();
  }
}
