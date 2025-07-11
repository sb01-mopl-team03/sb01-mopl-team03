package team03.mopl.domain.content.batch.sports;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SportsApiResponse {
  /**
   * API 응답 JSON의 "events" 배열과 맵핑
   */
  @JsonProperty("events")
  private List<SportsItemDto> events;
}
