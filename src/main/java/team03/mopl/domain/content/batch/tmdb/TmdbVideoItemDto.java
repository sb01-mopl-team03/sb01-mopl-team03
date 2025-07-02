package team03.mopl.domain.content.batch.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TmdbVideoItemDto {
  @JsonProperty("name")
  private String name;

  @JsonProperty("key")
  private String key;

  @JsonProperty("site")
  private String site;

  @JsonProperty("published_at")
  private String publishedAt;
}
