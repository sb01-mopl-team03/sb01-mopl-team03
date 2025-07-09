package team03.mopl.domain.subscription.dto;

import java.util.UUID;
import team03.mopl.domain.subscription.Subscription;

public record SubscriptionDto(
    UUID userId,
    UUID playlistId
) {

  public static SubscriptionDto from(Subscription subscription) {
    return new SubscriptionDto(
        subscription.getUser().getId(),
        subscription.getPlaylist().getId()
    );
  }
}
