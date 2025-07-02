package team03.mopl.domain.content.batch.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TmdbVideoApiResponse {

  @JsonProperty("id")
  private String id;

  /**
   * API 응답 JSON의 "results" 배열과 맵핑
   */
  @JsonProperty("results")
  private List<TmdbVideoItemDto> results;
}
