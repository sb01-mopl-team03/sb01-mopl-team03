package team03.mopl.domain.content.batch.tmdb;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;

@Slf4j
@RequiredArgsConstructor
public class InitialTmdbApiProcessor implements ItemProcessor<TmdbItemDto, Content> {

  private final RestTemplate restTemplate;
  private final String baseurl;
  private final String apiToken;

  @Override
  public Content process(TmdbItemDto item) throws Exception {

    // 1. URI 생성을 위한 videoType 확인
    String title = "";
    String videoType = "";
    ContentType contentType;
    LocalDateTime releaseDate = LocalDateTime.now();

    if (item.getFirstAirDate() != null) {
      videoType = "tv";
      title = item.getName();
      contentType = ContentType.TV;
      LocalDate localDate = LocalDate.parse(item.getFirstAirDate());
      releaseDate = localDate.atStartOfDay();
    } else {
      videoType = "movie";
      title = item.getTitle();
      contentType = ContentType.MOVIE;
      LocalDate localDate = LocalDate.parse(item.getReleaseDate());
      releaseDate = localDate.atStartOfDay();
    }

    // 2. URI 생성
    URI uri = UriComponentsBuilder
        .fromUriString(baseurl)
        .path("/{videoType}/{id}/videos")
        .queryParam("language", "ko-KR")
        .build(videoType, item.getId());
    log.info("uri={}", uri);

    // 3. Header 설정
    HttpHeaders headers = new org.springframework.http.HttpHeaders();
    headers.set("accept", "application/json");
    headers.set("Authorization", "Bearer " + apiToken);
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    // 4. RestTemplate으로 API를 호출, ResponseEntity<TmdbVideoApiResponse> 응답 받기
    ResponseEntity<TmdbVideoApiResponse> responseEntity = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        httpEntity,
        TmdbVideoApiResponse.class
    );
    // 5. responseEntity 에서 response 를 들고온다.
    TmdbVideoApiResponse tmdbVideoApiResponse =
        responseEntity.getBody() != null ? responseEntity.getBody() : null;

    // 7. Response에서 List<TmdbVideoItemDto>를 들고온다.
    TmdbVideoItemDto tmdbVideoItemDto =
        (tmdbVideoApiResponse.getResults() != null && !tmdbVideoApiResponse.getResults().isEmpty()) ? tmdbVideoApiResponse.getResults().get(0) : null;

    // 8. video URL 생성
    // 현재로선 전부 Youtube 인 것들만 확인되었습니다.
    String videoUrl = "";
    if (tmdbVideoItemDto != null && tmdbVideoItemDto.getSite().equals("YouTube")) {
      log.info("tmdbVideoItemDto.getType()={}", tmdbVideoItemDto.getSite());
      videoUrl = "https://youtu.be/" + tmdbVideoItemDto.getKey();
      log.info("videoUrl={}", videoUrl);
    }


    // 9. Content 객체 생성및 반환
    Content content = Content.builder()
        .title(title)
        .description(item.getOverview())
        .contentType(contentType)
        .releaseDate(releaseDate)
        .url(videoUrl)
        .build();

    return content;
  }
}
