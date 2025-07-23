package team03.mopl.domain.content.batch.sports;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.common.util.NormalizerUtil;
import team03.mopl.domain.content.repository.ContentRepository;

@Slf4j
@RequiredArgsConstructor
public class SportsApiProcessor implements ItemProcessor<SportsItemDto, Content> {

  private final ContentRepository contentRepository;

  @Override
  public Content process(SportsItemDto item) throws Exception {
    log.info("SportsApiProcessor - SPORTS 아이템 → 컨텐츠 변환 시작 : fileName={}",
        item.getStrFilename());

    if (contentRepository.existsByTitle(item.getStrFilename())) {
      log.debug("이미 존재하는 컨텐츠입니다.: item.getStrFilename()={}", item.getStrFilename());
      return null;
    }

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
    log.debug("Description 생성 완료: description={}",
        description.length() > 30 ? description.substring(0, 30) + "..." : description);

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
      log.debug("DateTime 변환 완료: originalDate={}, originalTime={}, kstDateTime={}", date, time,
          dateTime);
    }

    // 3. title 문자열 정규화
    String titleNormalized = NormalizerUtil.normalize(item.getStrFilename());
    log.debug("Title 정규화 완료: original={}, normalized={}", item.getStrFilename(), titleNormalized);

    // 4. item.getStrVideo() 여부 확인
    String strVideo = "";
    if (item.getStrVideo() != null) {
      strVideo = item.getStrVideo();
    }
    log.debug("비디오 URL 확인: strVideo={}", strVideo);

    // 5. content 객체 생성및 반환
    Content content = Content.builder()
        .title(item.getStrFilename())
        .titleNormalized(titleNormalized)
        .description(description.toString())
        .contentType(ContentType.SPORTS)
        .releaseDate(dateTime)
        .youtubeUrl(strVideo)
        .thumbnailUrl(item.getStrThumb())
        .build();

    log.info("SportsApiProcessor - 아이템 처리 성공, Writer로 전달: fileName={}", item.getStrFilename());

    return content;
  }
}
