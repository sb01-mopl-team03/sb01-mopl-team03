package team03.mopl.domain.content.batch.tmdb;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
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


/**
 * top_rated API 를 호출한다.
 */
@Slf4j
public class InitialTmdbApiReader implements ItemStreamReader<TmdbItemDto> {

  private final RestTemplate restTemplate;
  private final List<TmdbApiRequestInfo> apiRequestInfos;
  private final String baseurl;
  private final String apiToken;
  private int firstPage = 1;
  private int lastPage = 21;
  private int nextRequestIndex = 0;
  private List<TmdbItemDto> tmdbItemDtos;
  private int nextItemIndex = 0;

  public InitialTmdbApiReader(RestTemplate restTemplate, String baseurl, String apiToken) {
    this.restTemplate = restTemplate;
    this.apiToken = apiToken;
    this.apiRequestInfos = buildApiRequestInfo();
    this.baseurl = baseurl;
  }


  @Override
  public TmdbItemDto read()
      throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    while (true) {
      // 1. 읽을 Item이 있는지 확인
      if (tmdbItemDtos == null || nextItemIndex >= tmdbItemDtos.size()) {
        // 2. 다음 API가 없다면 false 반환
        if (!fetchTmdbFromApi()) {
          // 2*. null을 반환한다.
          return null;
        }
      }
      if (!tmdbItemDtos.isEmpty() && nextItemIndex < tmdbItemDtos.size()) {
        // 3. Item 을 반환한다.
        return tmdbItemDtos.get(nextItemIndex++);
      }
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
    TmdbApiRequestInfo info = apiRequestInfos.get(nextRequestIndex);
    nextRequestIndex++;

    // 3. info 정보로 API URI 생성한다.
    URI uri = UriComponentsBuilder
        .fromUriString(baseurl)
        .path("/{videoType}/top_rated")
        .queryParam("language", "ko-KR")
        .queryParam("page", info.getPage())
        .build(info.getVideoType());
    log.info("uri={}", uri);

    // 4. Header를 설정한다.
    HttpHeaders headers = new HttpHeaders();
    headers.set("accept", "application/json");
    headers.set("Authorization", "Bearer " + apiToken);
    HttpEntity<String> httpEntity = new HttpEntity<>(headers);

    // 5. RestTemplate으로 API를 호출하고 ResponseEntity<TmdbApiResponse> 객체로 받는다.
    ResponseEntity<TmdbApiResponse> responseEntity = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        httpEntity,
        TmdbApiResponse.class
    );
    // 6. responseEntity에서 response를 들고온다.
    TmdbApiResponse response = responseEntity.getBody() != null ?
        responseEntity.getBody() : null;

    // 7.  Response에서 List<TmdbItemDto>를 꺼낸다.
    this.tmdbItemDtos = (response != null && response.getResults() != null) ? response.getResults()
        : new ArrayList<>();

    // 8. 초기화로 인한 nextItemIndex 초기화
    this.nextItemIndex = 0;

    return true;
  }

  /**
   * API 호출을 위한 Info 객체 생성
   * <p>
   * Top Rated 의 1~20페이지
   */
  public List<TmdbApiRequestInfo> buildApiRequestInfo() {
    List<TmdbApiRequestInfo> requestInfos = new ArrayList<>();
    List<String> videoTypes = List.of("movie", "tv");
    List<Integer> pageNumbers = IntStream.range(firstPage, lastPage)
        .boxed()
        .toList();
    for (String videoType : videoTypes) {
      for (Integer pageNumber : pageNumbers) {
        requestInfos.add(TmdbApiRequestInfo.builder()
            .videoType(videoType)
            .page(pageNumber.toString())
            .build());
      }
    }
    return requestInfos;
  }

  // -----------------------------------------------------------------------------------------------

  /**
   * Step이 시작될 때, 또는 재시작할 때 호출되는 동작
   */
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if (executionContext.containsKey("nextRequestIndex")) {
      this.nextRequestIndex = executionContext.getInt("nextRequestIndex");
      log.info("Job 재시작: " + this.nextRequestIndex + "번째 요청부터 다시 시작합니다.");
    } else {
      this.nextRequestIndex = 0;
      log.info("Job 신규 시작");
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
  }

  @Override
  public void close() throws ItemStreamException {
    log.info("InitialTmdbApiReader 종료");
  }
}
