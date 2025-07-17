package team03.mopl.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;

@Schema(description = "콘텐츠 정보 DTO")
public record ContentDto (

    @Schema(description = "콘텐츠 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "콘텐츠 생성일시", example = "2025-07-16T10:15:30", type = "string", format = "date-time")
    LocalDateTime createdAt,

    @Schema(description = "콘텐츠 제목", example = "센과 치히로의 행방불명")
    String title,

    @Schema(description = "정규화된 콘텐츠 제목", example = "senwachihironohaengbangbulmyeong")
    String titleNormalized,

    @Schema(description = "콘텐츠 설명", example = "신비한 세계에서 펼쳐지는 소녀의 성장 이야기")
    String description,

    @Schema(description = "콘텐츠 타입", example = "MOVIE")
    ContentType contentType,

    @Schema(description = "출시일", example = "2001-07-20T00:00:00", type = "string", format = "date-time")
    LocalDateTime releaseDate,

    @Schema(description = "YouTube 영상 링크", example = "https://youtube.com/watch?v=abcd1234")
    String youtubeUrl,

    @Schema(description = "썸네일 이미지 URL", example = "https://cdn.mopl.com/thumbnails/content01.png")
    String thumbnailUrl

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
