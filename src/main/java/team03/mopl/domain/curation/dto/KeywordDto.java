package team03.mopl.domain.curation.dto;

import java.util.UUID;
import team03.mopl.domain.curation.entity.Keyword;

public record KeywordDto(
    UUID userId,
    String keyword
) {

  public static KeywordDto from(Keyword keyword) {
    return new KeywordDto(
        keyword.getUser().getId(),
        keyword.getKeyword()
    );
  }
}
