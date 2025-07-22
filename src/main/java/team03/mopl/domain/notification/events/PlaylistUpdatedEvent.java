package team03.mopl.domain.notification.events;

import java.util.List;
import java.util.UUID;

public record PlaylistUpdatedEvent(
    UUID playlistId,
    UUID ownerId,
    UUID updaterId,
    List<UUID> addedContentIds,
    String playlistTitle
) {}
