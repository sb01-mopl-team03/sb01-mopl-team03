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
  private List<SportsApiRequestInfo> apiRequestInfos;
  private final String baseUrl;
  private int nextRequestIndex = 0;
  private List<SportsItemDto> sportsItemDtos;
  private int nextItemIndex = 0;

  public SportsApiReader(RestTemplate restTemplate, String baseUrl) {
    this.restTemplate = restTemplate;
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
        log.debug("아이템 소진, API 데이터 패칭 시작");
        // 2. 다음 API가 없다면 false 반환
        if (!fetchSportsFromApi()) {
          log.debug("모든 API 요청 처리 완료, ItemReader 종료");
          // 2*. null을 반환한다.
          return null;
        }
      }
      if (!sportsItemDtos.isEmpty() && nextItemIndex < sportsItemDtos.size()) {
        SportsItemDto itemDto = sportsItemDtos.get(nextItemIndex++);
        String itemTitle = itemDto.getStrFilename();
        log.debug("아이템 읽기 성공: itemTitle={}", itemTitle);
        // 3. Item 을 반환한다.
        return itemDto;
      }
    }
  }

  /**
   * API 호출및 호출 여부 반환
   */
  private boolean fetchSportsFromApi() {
    // 1. info를 전부 순회했는지 확인
    if (nextRequestIndex >= apiRequestInfos.size()) {
      return false;
    }

    // 2. API 정보 꺼낸다.
    SportsApiRequestInfo info = apiRequestInfos.get(nextRequestIndex);
    nextRequestIndex++;

    log.debug("API 데이터 패칭 시작: leagueId={}, season={}", info.getLeagueId(), info.getSeason());
    // 3. info 객체 정보로 API URL 생성한다.
    URI uri = UriComponentsBuilder
        .fromUriString(baseUrl)
        .path("/{apiKey}/eventsday.php")
        .queryParam("d", info.getDate())
        .queryParam("l", info.getLeagueName())
        .build("123");

    log.debug("SPORTS API 요청하기: url={}", uri);

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
    log.debug("적재할 SPORTS 경기 일자: yesterday={}", yesterday);
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
    this.apiRequestInfos = buildApiRequestInfo();
    if (executionContext.containsKey("nextRequestIndex")) {
      this.nextRequestIndex = executionContext.getInt("nextRequestIndex");
      log.info("SportsApiReader - SPORTS API 데이터 읽기 재시작: nextRequestIndex={}",
          this.nextRequestIndex);
    } else {
      this.nextRequestIndex = 0;
      log.info("SportsApiReader - SPORTS API 데이터 읽기 신규 시작");
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
    log.info("SportsApiReader - SPORTS API 리더 리소스 정리 완료 (Step 종료 시 호출)");
  }
}
