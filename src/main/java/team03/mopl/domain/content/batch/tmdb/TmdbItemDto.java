package team03.mopl.domain.content.batch.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TmdbItemDto {

  @JsonProperty("id")
  private String id;

  @JsonProperty("title")
  private String title;

  @JsonProperty("overview")
  private String overview;

  @JsonProperty("release_date")
  private String releaseDate;

  @JsonProperty("name")
  private String name;

  @JsonProperty("first_air_date")
  private String firstAirDate;
}
