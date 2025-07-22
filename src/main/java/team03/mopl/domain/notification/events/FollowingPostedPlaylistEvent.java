package team03.mopl.domain.notification.events;

import java.util.UUID;

public record FollowingPostedPlaylistEvent(
  UUID creatorId,
  UUID playlistId,
  String playlistName,
  boolean isPublic
) {}

