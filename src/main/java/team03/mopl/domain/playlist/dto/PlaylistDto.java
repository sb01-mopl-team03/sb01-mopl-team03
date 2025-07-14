package team03.mopl.domain.playlist.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.entity.PlaylistContent;
import team03.mopl.domain.subscription.dto.SubscriptionDto;

public record PlaylistDto(
    UUID id,
    String name,
    UUID userId,
    String username,
    Boolean isPublic,
    LocalDateTime createdAt,
    List<ContentDto> playlistContents,
    List<SubscriptionDto> subscriptions
) {

  public static PlaylistDto from(Playlist playlist) {
    List<ContentDto> contentDtos = playlist.getPlaylistContents().stream()
        .map(PlaylistContent::getContent)
        .map(ContentDto::from)
        .toList();

    List<SubscriptionDto> subscriptionDtos = playlist.getSubscriptions().stream()
        .map(SubscriptionDto::from)
        .toList();

    return new PlaylistDto(
        playlist.getId(),
        playlist.getName(),
        playlist.getUser().getId(),
        playlist.getUser().getName(),
        playlist.isPublic(),
        playlist.getCreatedAt(),
        contentDtos,
        subscriptionDtos);
  }
}
