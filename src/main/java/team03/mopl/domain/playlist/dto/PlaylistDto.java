package team03.mopl.domain.playlist.dto;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.entity.PlaylistContent;
import team03.mopl.domain.subscription.Subscription;
import team03.mopl.domain.user.User;

public record PlaylistDto(
    UUID playlistId,
    String name,
    User user,
    Boolean isPublic,
    List<PlaylistContent> playlistContents,
    List<Subscription> subscriptions
) {

  public static PlaylistDto from(Playlist playlist) {
    return new PlaylistDto(
        playlist.getId(),
        playlist.getName(),
        playlist.getUser(),
        playlist.isPublic(),
        playlist.getPlaylistContents(),
        playlist.getSubscriptions());
  }
}
