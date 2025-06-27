package team03.mopl.domain.content.batch.sports;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class SportsItemDto {

  @JsonProperty("strFilename")
  private String strFilename;

  @JsonProperty("strVenue")
  private String strVenue;

  @JsonProperty("strLeague")
  private String strLeague;

  @JsonProperty("strHomeTeam")
  private String strHomeTeam;

  @JsonProperty("strAwayTeam")
  private String strAwayTeam;

  @JsonProperty("homeScore")
  private String homeScore;

  @JsonProperty("awayScore")
  private String awayScore;

  @JsonProperty("dateEvent")
  private String dateEvent;

  @JsonProperty("strTime")
  private String strTime;

  @JsonProperty("strVideo")
  private String strVideo;

}
