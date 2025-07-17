package team03.mopl.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.entity.PlaylistContent;
import team03.mopl.domain.subscription.dto.SubscriptionDto;

@Schema(description = "플레이리스트 응답 DTO")
public record PlaylistDto(
    @Schema(description = "플레이리스트 ID", example = "uuid-1")
    UUID id,

    @Schema(description = "플레이리스트 이름", example = "출근길 팝송")
    String name,

    @Schema(description = "유저 ID", example = "uuid-2")
    UUID userId,

    @Schema(description = "유저 이름", example = "홍길동")
    String username,

    @Schema(description = "공개 여부", example = "true")
    Boolean isPublic,

    @Schema(description = "생성일시", example = "2025-07-16T09:00:00")
    LocalDateTime createdAt,

    @Schema(description = "포함된 콘텐츠 목록")
    List<ContentDto> playlistContents,

    @Schema(description = "구독 정보 목록")
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
