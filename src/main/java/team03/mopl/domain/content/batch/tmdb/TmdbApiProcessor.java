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
import team03.mopl.common.util.NormalizerUtil;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.content.ContentType;

@Slf4j
@RequiredArgsConstructor
public class TmdbApiProcessor implements ItemProcessor<TmdbItemDto, Content> {

  private final ContentRepository contentRepository;
  private final RestTemplate restTemplate;
  private final String baseurl;
  private final String apiToken;

  @Override
  public Content process(TmdbItemDto item) throws Exception {
    log.info("TmdbApiProcessor - TMDB 아이템 → 컨텐츠 변환 시작 : item.getId={}", item.getId());

    if (contentRepository.existsByDataId(item.getId())) {
      log.debug("이미 존재하는 컨텐츠입니다.: item.getId()={}", item.getId());
      return null;
    }

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
    log.debug("기본 정보 추출 완료: type={}, title={}", contentType, title);

    // 2. URI 생성
    URI uri = UriComponentsBuilder
        .fromUriString(baseurl)
        .path("/{videoType}/{id}/videos")
        .queryParam("language", "ko-KR")
        .build(videoType, item.getId());
    log.debug("비디오 정보 API 요청 시작: url={}", uri);

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
        (tmdbVideoApiResponse.getResults() != null && !tmdbVideoApiResponse.getResults().isEmpty())
            ? tmdbVideoApiResponse.getResults().get(0) : null;

    // 8. video URL 생성
    // 현재로선 전부 Youtube 인 것들만 확인되었습니다.
    String videoUrl = "";
    if (tmdbVideoItemDto != null && tmdbVideoItemDto.getSite().equals("YouTube")) {
      videoUrl = "https://youtu.be/" + tmdbVideoItemDto.getKey();
      log.debug("비디오 URL 추출 성공: videoUrl={}", videoUrl);
    }

    if (videoUrl.isEmpty()) {
      log.debug("YouTube URL 부재로 스킵: itemId={}", item.getId());
      return null;
    }

    // 9. title 문자열 정규화
    String titleNormalized = NormalizerUtil.normalize(title);

    // 10. thumbnail URL 생성: 전체 경로, 원본 이미지
    String thumbnailUrl = "";
    if (item.getPosterPath() != null) {
      thumbnailUrl = "https://image.tmdb.org/t/p/original" + item.getPosterPath();
    }

    // 11. Content 객체 생성및 반환
    Content content = Content.builder()
        .title(title)
        .titleNormalized(titleNormalized)
        .dataId(item.getId())
        .description(item.getOverview())
        .contentType(contentType)
        .releaseDate(releaseDate)
        .youtubeUrl(videoUrl)
        .thumbnailUrl(thumbnailUrl)
        .build();

    log.info("TmdbApiProcessor - 아이템 처리 성공, Writer로 전달: dataId={}", item.getId());

    return content;
  }
}
