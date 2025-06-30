package team03.mopl.domain.content.batch.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TmdbApiResponse {

  @JsonProperty("page")
  private int page;

  /**
   * API 응답 JSON의 "results" 배열과 맵핑
   */
  @JsonProperty
  private List<TmdbItemDto> results;
}
