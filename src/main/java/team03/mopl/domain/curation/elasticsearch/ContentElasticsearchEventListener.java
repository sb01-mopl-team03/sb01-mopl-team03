package team03.mopl.domain.curation.elasticsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async; // 비동기 처리를 위해 필요
import org.springframework.stereotype.Component;
import team03.mopl.domain.content.ContentChangeEvent; // 위에서 생성한 이벤트 클래스 임포트

// Content 변경 이벤트를 수신하여 Elasticsearch 작업을 수행하는 리스너
@Slf4j
@Component
@RequiredArgsConstructor
public class ContentElasticsearchEventListener {

  private final ContentSearchService contentSearchService;

  // ContentChangeEvent를 비동기적으로 처리 (DB 트랜잭션에 영향을 주지 않도록)
  @EventListener
  @Async // 이 메서드를 비동기적으로 실행하여 DB 저장 트랜잭션을 지연시키지 않습니다.
  public void handleContentChangeEvent(ContentChangeEvent event) {
    try {
      if (event.getType() == ContentChangeEvent.Type.SAVE) {
        log.info("[Elasticsearch Event] Content SAVE/UPDATE event for ID: {}", event.getContentId());
        // ContentSearchService의 indexContent 메서드가 upsert (없으면 추가, 있으면 업데이트) 기능을 한다면 이대로 사용
        contentSearchService.indexContent(event.getContent());
      } else if (event.getType() == ContentChangeEvent.Type.DELETE) {
        log.info("[Elasticsearch Event] Content DELETE event for ID: {}", event.getContentId());
        contentSearchService.deleteContent(event.getContentId()); // ContentSearchService에 deleteContent 메서드 추가 필요
      }
    } catch (Exception e) {
      log.error("[Elasticsearch Event Error] Failed to process ContentChangeEvent for ID: {}, Type: {}. Error: {}",
          event.getContentId(), event.getType(), e.getMessage(), e);
      // TODO: 실패한 이벤트를 재시도할 수 있는 메커니즘 (예: Dead Letter Queue, 재시도 로직) 구현 고려
    }
  }
}
