package team03.mopl.domain.curation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import team03.mopl.domain.content.dto.ContentDto;

@Builder
public record ContentWithScoreDto(
    @JsonProperty("content") ContentDto content,
    @JsonProperty("score") double score,
    @JsonProperty("contentId") String contentId
) {

  public static ContentWithScoreDto from(ContentDto content, double score) {
    return ContentWithScoreDto.builder()
        .content(content)
        .score(score)
        .contentId(content.id().toString())
        .build();
  }
}
