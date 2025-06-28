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

  @JsonProperty("intHomeScore")
  private String intHomeScore;

  @JsonProperty("intAwayScore")
  private String intAwayScore;

  @JsonProperty("dateEvent")
  private String dateEvent;

  @JsonProperty("strTime")
  private String strTime;

  @JsonProperty("strTimeLocal")
  private String strTimeLocal;

  @JsonProperty("strVideo")
  private String strVideo;

}
