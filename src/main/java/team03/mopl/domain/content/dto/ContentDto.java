package team03.mopl.domain.content.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;

public record ContentDto (
    UUID id,
    LocalDateTime createdAt,
    String title,
    String titleNormalized,
    String description,
    ContentType contentType,
    LocalDateTime releaseDate,
    String youtubeUrl,
    String thumbnailUrl,
    BigDecimal avgRating
){
  public static ContentDto from(Content content){
    return new ContentDto(
        content.getId(),
        content.getCreatedAt(),
        content.getTitle(),
        content.getTitleNormalized(),
        content.getDescription(),
        content.getContentType(),
        content.getReleaseDate(),
        content.getYoutubeUrl(),
        content.getThumbnailUrl(),
        content.getAvgRating()
    );
  }
}
