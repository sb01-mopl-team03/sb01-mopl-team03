package team03.mopl.domain.subscription.dto;

import java.util.UUID;

public record SubscribeRequest(
    UUID userId,
    UUID playlistId
) {
}
