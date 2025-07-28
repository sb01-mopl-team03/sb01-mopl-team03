package team03.mopl.domain.content.batch.tmdb;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class TmdbApiReader implements ItemStreamReader<TmdbItemDto> {

  private final RestTemplate restTemplate;
  private final String baseurl;
  private final String apiToken;
  private List<TmdbApiRequestInfo> apiRequestInfos;
  private int nextRequestIndex = 0;
  private List<TmdbItemDto> tmdbItemDtos;
  private int nextItemIndex = 0;
  private boolean initialized = false;

  public TmdbApiReader(RestTemplate restTemplate, String baseurl, String apiToken) {
    this.restTemplate = restTemplate;
    this.baseurl = baseurl;
    this.apiToken = apiToken;
  }

  @Override
  public TmdbItemDto read()
      throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

    // 1. request info 객체 생성을 위한 초기화
    if (!initialized) {
      initializeApiRequests();
      initialized = true;
    }

    while (true) {
      // 2. 읽을 Item이 있는지 확인
      if (tmdbItemDtos == null || nextItemIndex >= tmdbItemDtos.size()) {
        log.debug("아이템 소진, API 데이터 패칭 시작");
        // 3. 다음 API가 없다면 false 반환
        if (!fetchTmdbFromApi()) {
          log.debug("모든 API 요청 처리 완료, ItemReader 종료");
          return null;
        }
      }

      if (!tmdbItemDtos.isEmpty() && nextItemIndex <= tmdbItemDtos.size()) {
        TmdbItemDto itemDto = tmdbItemDtos.get(nextItemIndex++);
        String itemTitle =
            itemDto.getTitle() != null ? itemDto.getTitle() : itemDto.getName();
        log.debug("아이템 읽기 성공: itemTitle={}", itemTitle);
        // 3. Item을 반환한다.
        return itemDto;
      }
    }
  }

  /**
   * API 호출을 위한 모든 Info 객체 생성
   * <p>
   * 첫 번째 페이지를 호출하여 총 페이지 수를 확인하고 Info 객체를 생성한다.
   */
  public void initializeApiRequests() {
    log.debug("TMDB API 요청 정보 초기화를 시작");
    this.apiRequestInfos = new ArrayList<>();

    // 1. Movie, tv 데이터 준비
    List<String> videoTypes = List.of("movie", "tv");

    for (String videoType : videoTypes) {
      log.debug("타입별 초기화 시작: videoType={}", videoType);
      try {
        // 2. 각 타입별로 첫 번째 페이지 호출
        TmdbApiResponse firstPageResponse = callFirstPage(videoType);

        if (firstPageResponse != null) {
          int totalPages = Integer.parseInt(firstPageResponse.getTotalPages());
          log.debug("첫 페이지 API 응답 성공: totalPages={}", totalPages);
          // 3. 1p ~ totalPage 까지 모든 request info 생성
          for (int page = 1; page <= totalPages; page++) {
            apiRequestInfos.add(TmdbApiRequestInfo.builder()
                .videoType(videoType)
                .page(String.valueOf(page))
                .build());
          }
        }
      } catch (Exception e) {
        log.debug("{} 초기화 중 오류 발생", videoType, e);
      }
    }
    log.info("TMDB API 요청 정보 초기화 완료. 총 생성된 요청 수: {}", apiRequestInfos.size());
  }

  /**
   * 각 비디오 타입별로 첫 번째 페이지 반환
   */
  public TmdbApiResponse callFirstPage(String videoType) {
    log.debug("첫 페이지 API 호출 시작: videoType={}", videoType);
    URI uri = buildApiUri(videoType, 1);

    // 1. Header를 설정한다.
    HttpHeaders headers = new HttpHeaders();
    headers.set("accept", "application/json");
    headers.set("Authorization", "Bearer " + apiToken);
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    try {
      ResponseEntity<TmdbApiResponse> responseEntity = restTemplate.exchange(uri, HttpMethod.GET,
          httpEntity, TmdbApiResponse.class);
      log.debug("첫 페이지 API 호출 성공: responseBodyExists={}", responseEntity.getBody() != null);
      return responseEntity.getBody();

    } catch (Exception e) {
      log.error("첫 번째 페이지 호출 실패", e);
      return null;
    }
  }

  /**
   * API 호출및 호출 여부 반환
   */
  public boolean fetchTmdbFromApi() {
    // 1. info를 전부 순회했는지 확인
    if (nextRequestIndex >= apiRequestInfos.size()) {
      return false;
    }

    // 2. API 정보를 꺼낸다.
    TmdbApiRequestInfo requestInfo = apiRequestInfos.get(nextRequestIndex);
    nextRequestIndex++;

    log.debug("API 데이터 패칭 시작: videoType={}, page={}", requestInfo.getVideoType(),
        requestInfo.getPage());
    // 3. URI 생성
    URI uri = buildApiUri(requestInfo.getVideoType(), Integer.parseInt(requestInfo.getPage()));

    // 4. Header를 설정한다.
    HttpHeaders headers = new HttpHeaders();
    headers.set("accept", "application/json");
    headers.set("Authorization", "Bearer " + apiToken);
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    try {
      // 5. RestTemplate으로 API를 호출하고 ResponseEntity<TmdbApiResponse> 객체로 받는다.
      ResponseEntity<TmdbApiResponse> responseEntity = restTemplate.exchange(
          uri,
          HttpMethod.GET,
          httpEntity,
          TmdbApiResponse.class);

      log.debug("TMDB API 요청하기: url={}", uri);

      // 6. responseEntity에서 response를 들고온다.
      TmdbApiResponse response = responseEntity.getBody() != null ? responseEntity.getBody() : null;

      // 7. Response에서 List<TmdbItemDto>를 꺼낸다.
      if (response != null && response.getResults() != null) {
        this.tmdbItemDtos = response.getResults();
        log.debug("API 데이터 패칭 성공: itemCount={}", this.tmdbItemDtos.size());
      } else {
        this.tmdbItemDtos = new ArrayList<>();
        log.debug("API 데이터 패칭 성공했으나, 응답 내용이 비어있음.");
      }

      // 8. 초기화로 인한 nextItemIndex 초기화
      this.nextItemIndex = 0;

      return true;

    } catch (Exception e) {
      log.error("API 데이터 패칭 실패: videoType={}, page={}, error={}",
          requestInfo.getVideoType(), requestInfo.getPage(), e.getMessage(), e);
      // 9. 실패시에도 다음 작업으로 넘어갈 수 있도록 true 반환
      return true;
    }
  }


  /**
   * URI를 생성하는 메서드
   * <p>
   * videoType에 따른 런타임에 동적으로 생성합니다.
   *
   * @param videoType movie, tv
   * @param page      페이징되어 반환되는 값의 페이지 개수
   */
  public URI buildApiUri(String videoType, int page) {
    UriComponentsBuilder builder;

    if ("movie".equals(videoType)) {
      builder = UriComponentsBuilder
          .fromUriString(baseurl)
          .path("/movie/now_playing")
          .queryParam("language", "ko-KR")
          .queryParam("page", page)
          .queryParam("region", "KR");

    } else if ("tv".equals(videoType)) {
      LocalDate today = LocalDate.now();
      LocalDate weekAgo = today.minusDays(7);

      builder = UriComponentsBuilder
          .fromUriString(baseurl)
          .path("/discover/tv")
          .queryParam("air_date.gte", weekAgo)
          .queryParam("air_date.lte", today)
          .queryParam("include_adult", "false")
          .queryParam("include_null_first_air_dates", "false")
          .queryParam("language", "ko-KR")
          .queryParam("page", page)
          .queryParam("sort_by", "popularity.desc")
          .queryParam("with_origin_country", "KR");

    } else {
      throw new IllegalArgumentException("지원하지 않는 비디오 타입 : " + videoType);
    }

    URI uri = builder.build().toUri();
    log.debug("API URI 생성 완료: uri={}", uri);
    return uri;
  }

  // -----------------------------------------------------------------------------------------------

  /**
   * Step이 시작될 때, 또는 재시작할 때 호출되는 동작
   */
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (executionContext.containsKey("nextRequestIndex")) {
      this.nextRequestIndex = executionContext.getInt("nextRequestIndex");
      log.info("TmdbApiReader - TMDB API 데이터 읽기 재시작: nextRequestIndex={}", this.nextRequestIndex);
    } else {
      this.nextRequestIndex = 0;
      log.info("TmdbApiReader - TMDB API 데이터 읽기 신규 시작");
    }

    if (executionContext.containsKey("nextItemIndex")) {
      this.nextItemIndex = executionContext.getInt("nextItemIndex");
    } else {
      this.nextItemIndex = 0;
    }
  }

  /**
   * Chunk 처리가 끝날 때마다 호출하여 상태를 저장한다.
   */
  @Override
  public void update(ExecutionContext executionContext) throws ItemStreamException {
    executionContext.putInt("nextRequestIndex", this.nextRequestIndex);
    executionContext.putInt("nextItemIndex", this.nextItemIndex);
    log.debug("ExecutionContext 업데이트 중: nextRequestIndex={}, nextItemIndex={}",
        this.nextRequestIndex, this.nextItemIndex);
  }

  @Override
  public void close() throws ItemStreamException {
    log.info("TmdbApiReader - TMDB API 리더 리소스 정리 완료 (Step 종료 시 호출)");
  }
}
