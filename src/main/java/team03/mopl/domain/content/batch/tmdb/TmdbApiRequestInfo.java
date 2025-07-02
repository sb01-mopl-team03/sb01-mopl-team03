package team03.mopl.domain.content.batch.tmdb;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TmdbApiRequestInfo {
  private String videoType;
  private String page;
}
