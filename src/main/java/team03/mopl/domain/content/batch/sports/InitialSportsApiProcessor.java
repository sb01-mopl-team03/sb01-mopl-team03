package team03.mopl.domain.content.batch.sports;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.springframework.batch.item.ItemProcessor;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;

public class InitialSportsApiProcessor implements ItemProcessor<SportsItemDto, Content> {

  @Override
  public Content process(SportsItemDto item) throws Exception {

    // 1. description 준비
    StringBuilder description = new StringBuilder();
    if (item.getStrLeague() != null) {
      description.append("리그: ").append(item.getStrLeague()).append("\n");
    }
    if (item.getStrVenue() != null) {
      description.append("장소: ").append(item.getStrVenue()).append("\n\n");
    }
    if (item.getStrHomeTeam() != null && item.getStrAwayTeam() != null) {
      description.append(item.getStrHomeTeam()).append("vs").append(item.getStrAwayTeam())
          .append("\n");
    }
    if (item.getHomeScore() != null && item.getAwayScore() != null) {
      description.append(item.getHomeScore()).append(":").append(item.getAwayScore());
    }
    String date = item.getDateEvent();
    String time = item.getStrTime();
    LocalDateTime dateTime = null;
    if (date != null || !date.isEmpty() || time != null || !time.isEmpty()) {
      dateTime = LocalDateTime.of(
          LocalDate.parse(date),
          LocalTime.parse(time)
      );
    }

    // 2. content 객체 생성및 반환
    Content content = Content.builder()
        .title(item.getStrFilename())
        .description(description.toString())
        .contentType(ContentType.SPORTS)
        .releaseDate(dateTime)
        .url(item.getStrVideo())
        .build();

    return content;
  }
}
