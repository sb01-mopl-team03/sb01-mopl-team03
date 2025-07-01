package team03.mopl.domain.content.batch.sports;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
public class SportsApiReader implements ItemStreamReader<SportsItemDto> {

  private final RestTemplate restTemplate;
  private final List<SportsApiRequestInfo> apiRequestInfos;
  private final String baseUrl;
  private int nextRequestIndex = 0;
  private List<SportsItemDto> sportsItemDtos;
  private int nextItemIndex = 0;

  public SportsApiReader(RestTemplate restTemplate, String baseUrl) {
    this.restTemplate = restTemplate;
    this.apiRequestInfos = buildApiRequestInfo();
    this.baseUrl = baseUrl;
    this.sportsItemDtos = new ArrayList<>();
  }

  @Override
  public SportsItemDto read()
      throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
    // API 호출시 빈 리스트를 대비하여 while(true)를 통해 회피한다
    while (true) {
      // 1. 읽을 Item이 있는지 확인
      if (sportsItemDtos == null || nextItemIndex >= sportsItemDtos.size()) {
        // 2. 다음 API가 없다면 false 반환
        if (!fetchSportsFromApi()) {
          // 2*. null을 반환한다.
          return null;
        }
      }
      if (!sportsItemDtos.isEmpty() && nextItemIndex < sportsItemDtos.size()) {
        // 3. Item 을 반환한다.
        return sportsItemDtos.get(nextItemIndex++);
      }
    }
  }

  private boolean fetchSportsFromApi() {
    // 1. Info를 전부 순회했는지 확인
    if (nextRequestIndex >= apiRequestInfos.size()) {
      return false;
    }

    // 2. API 정보 꺼낸다.
    SportsApiRequestInfo info = apiRequestInfos.get(nextRequestIndex);
    nextRequestIndex++;

    // 3. info 객체 정보로 API URL 생성한다.
    URI uri = UriComponentsBuilder
        .fromUriString(baseUrl)
        .path("/{apiKey}/eventsday.php")
        .queryParam("d", info.getDate())
        .queryParam("l", info.getLeagueName())
        .build("123");
    log.info("uri={}", uri);

    // 4. RestTemplate으로 API를 호출하고 SportsApiResponse 객체로 받는다.
    SportsApiResponse response = restTemplate.getForObject(uri, SportsApiResponse.class);

    // 5. 받은 Response에서 List<SportsItemDto>를 꺼낸다.
    this.sportsItemDtos = (response != null && response.getEvents() != null) ? response.getEvents()
        : new ArrayList<>();

    // 6. sportsItemDtos 초기화로 인한 nextEventIndex 초기화
    this.nextItemIndex = 0;

    return true;
  }

  /**
   * API 호출을 위한 Info 객체 생성
   * <p>
   * MLB, KBO | 어제 날짜
   */
  private List<SportsApiRequestInfo> buildApiRequestInfo() {
    List<SportsApiRequestInfo> requestInfos = new ArrayList<>();
    String yesterday = LocalDate.now().minusDays(1).toString();
    log.info("yesterday={}", yesterday);
    List<String> leagues = List.of("MLB", "Korean KBO League");
    for (String league : leagues) {
      requestInfos.add(SportsApiRequestInfo.builder()
          .date(yesterday)
          .leagueName(league)
          .build());
    }
    return requestInfos;
  }

  // -----------------------------------------------------------------------------------------------

  /**
   * Step이 시작될 때, 또는 재시작할 때 호출되는 동작
   */
  @Override
  public void open(ExecutionContext executionContext) throws ItemStreamException {
    if(executionContext.containsKey("nextRequestIndex")){
      this.nextRequestIndex = executionContext.getInt("nextRequestIndex");
      log.info("Job 재시작: " + this.nextRequestIndex + "번째 요청부터 다시 시작합니다.");
    } else {
      this.nextRequestIndex = 0;
      log.info("Job 신규 시작");
    }

    if (executionContext.containsKey("nextItemIndex")){
      this.nextItemIndex = executionContext.getInt("nextItemIndex");
    } else {
      this.nextItemIndex = 0;
    }
  }

  /**
   * Chunk 처리가 끝날 때마다 호출하여 상태를 저장한다.
   * */
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
