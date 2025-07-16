package team03.mopl.domain.curation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import team03.mopl.domain.curation.entity.Keyword;

@Schema(description = "키워드 응답 DTO")
public record KeywordDto(

    @Schema(description = "키워드 ID", example = "f12a34bc-56d7-8901-23ef-456789abcdef")
    UUID keywordId,

    @Schema(description = "사용자 ID", example = "abc123ef-4567-89ab-cdef-0123456789ab")
    UUID userId,

    @Schema(description = "키워드", example = "다큐멘터리")
    String keyword

) {
  public static KeywordDto from(Keyword keyword) {
    return new KeywordDto(
        keyword.getId(),
        keyword.getUser().getId(),
        keyword.getKeyword()
    );
  }
}
