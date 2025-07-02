package team03.mopl.domain.content.batch.sports;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SportsApiRequestInfo{
  // Sports API init 정보
  private String leagueId;
  private String season;
  // Sports API 주기적 적재 저옵
  private String leagueName;
  private String date;
}
