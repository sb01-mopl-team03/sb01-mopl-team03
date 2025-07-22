package team03.mopl.domain.notification.events;

import java.util.UUID;

public record PlaylistUpdatedEvent(
    UUID playlistId,
    UUID ownerId,
    String playlistTitle
) {}
