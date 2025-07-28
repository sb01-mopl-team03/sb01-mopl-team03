package team03.mopl.domain.curation.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import team03.mopl.domain.content.Content;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentSearch {

  @JsonProperty("id")
  private String id;

  @JsonProperty("title")
  private String title;

  @JsonProperty("description")
  private String description;

  @JsonProperty("content_type")
  private String contentType;

  @JsonProperty("avg_rating")
  private Double avgRating;

  public static ContentSearch from(Content content) {
    return ContentSearch.builder()
        .id(content.getId().toString())
        .title(content.getTitle())
        .description(content.getDescription())
        .contentType(content.getContentType().toString())
        .avgRating(content.getAvgRating().doubleValue())
        .build();
  }
}
