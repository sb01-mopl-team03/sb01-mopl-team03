package team03.mopl.domain.curation.dto;

import java.util.UUID;
import team03.mopl.domain.curation.entity.Keyword;

public record KeywordDto(
    UUID keywordId,
    UUID userId,
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
