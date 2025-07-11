package team03.mopl.domain.content.batch.sports;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.batch.item.ItemProcessor;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.common.util.NormalizerUtil;

public class SportsApiProcessor implements ItemProcessor<SportsItemDto, Content> {

  @Override
  public Content process(SportsItemDto item) throws Exception {

    // 1. description 생성
    StringBuilder description = new StringBuilder();
    if (item.getStrLeague() != null) {
      description.append("리그: ").append(item.getStrLeague()).append("\n");
    }
    if (item.getStrVenue() != null) {
      description.append("장소: ").append(item.getStrVenue()).append("\n\n");
    }
    if (item.getStrHomeTeam() != null && item.getStrAwayTeam() != null) {
      description.append(item.getStrHomeTeam()).append(" vs ").append(item.getStrAwayTeam())
          .append("\n");
    }
    if (item.getIntHomeScore() != null && item.getIntAwayScore() != null) {
      description.append(item.getIntHomeScore()).append(":").append(item.getIntAwayScore());
    }

    // 2. dateTime 생성
    String date = item.getDateEvent();
    String time = item.getStrTime();
    LocalDateTime dateTime = null;
    if (date != null || !date.isEmpty() || time != null || !time.isEmpty()) {
      LocalDate localDate = LocalDate.parse(date);
      LocalTime localTime = LocalTime.parse(time);

      ZonedDateTime utcDateTime = ZonedDateTime.of(localDate, localTime, ZoneId.of("UTC"));
      ZonedDateTime kstDateTime = utcDateTime.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
      dateTime = kstDateTime.toLocalDateTime();
    }

    // 3. title 문자열 정규화
    String titleNormalized = NormalizerUtil.normalize(item.getStrFilename());

    // 2. content 객체 생성및 반환
    Content content = Content.builder()
        .title(item.getStrFilename())
        .titleNormalized(titleNormalized)
        .description(description.toString())
        .contentType(ContentType.SPORTS)
        .releaseDate(dateTime)
        .url(item.getStrVideo())
        .build();

    return content;
  }
}
