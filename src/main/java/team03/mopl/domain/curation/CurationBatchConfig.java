//package team03.mopl.domain.curation;
//
//import java.util.List;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import team03.mopl.domain.content.Content;
//import team03.mopl.domain.content.repository.ContentRepository;
//import team03.mopl.domain.curation.service.CurationService;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class CurationBatchConfig {
//
//  private final CurationService curationService;
//  private final ContentRepository contentRepository;
//
//  /**
//   * 모든 콘텐츠에 대한 큐레이션 재계산 배치 작업
//   */
//  public void executeFullCurationBatch() {
//    log.info("executeFullCurationBatch - 전체 큐레이션 재계산 시작");
//
//    try {
//      final int BATCH_SIZE = 100;
//      int offset = 0;
//      int totalProcessed = 0;
//
//      while (true) {
//        List<Content> contentBatch = contentRepository.findAllWithOffset(offset, BATCH_SIZE);
//
//        if (contentBatch.isEmpty()) {
//          break;
//        }
//
//        // 배치 단위로 큐레이션 수행
//        curationService.batchCurationForNewContents(contentBatch);
//
//        totalProcessed += contentBatch.size();
//        offset += BATCH_SIZE;
//
//        log.info("executeFullCurationBatch - 진행 상황: {}개 처리", totalProcessed);
//
//        // 시스템 부하 방지를 위한 대기
//        Thread.sleep(1000);
//      }
//
//      log.info("executeFullCurationBatch - 전체 큐레이션 재계산 완료: {}개 처리", totalProcessed);
//
//    } catch (Exception e) {
//      log.warn("executeFullCurationBatch - 전체 큐레이션 재계산 실패", e);
//    }
//  }
//
//  /**
//   * 특정 기간 내 생성된 콘텐츠에 대한 큐레이션 배치 작업
//   */
//  public void executeRecentContentsCuration(int daysBack) {
//    log.info("executeRecentContentsCuration - 최근 {}일 콘텐츠 큐레이션 시작", daysBack);
//
//    try {
//      List<Content> recentContents = contentRepository.findRecentContents(daysBack);
//
//      if (!recentContents.isEmpty()) {
//        curationService.batchCurationForNewContents(recentContents);
//        log.info("executeRecentContentsCuration - 최근 콘텐츠 큐레이션 완료: {}개", recentContents.size());
//      } else {
//        log.info("executeRecentContentsCuration - 최근 {}일 내 콘텐츠가 없습니다", daysBack);
//      }
//
//    } catch (Exception e) {
//      log.warn("executeRecentContentsCuration - 최근 콘텐츠 큐레이션 실패", e);
//    }
//  }
//}
