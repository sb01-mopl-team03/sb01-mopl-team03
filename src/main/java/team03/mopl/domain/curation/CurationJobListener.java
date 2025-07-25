package team03.mopl.domain.curation;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.stereotype.Component;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
//import team03.mopl.domain.curation.service.CurationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurationJobListener implements JobExecutionListener {
//
//  private final CurationService curationService;
//  private final ContentRepository contentRepository;
//
//  @BeforeJob
//  public void beforeJob(JobExecution jobExecution) {
//    String jobName = jobExecution.getJobInstance().getJobName();
//    log.info("CurationJobListener - 배치 작업 시작: {}", jobName);
//  }
//
//  @AfterJob
//  public void afterJob(JobExecution jobExecution) {
//    String jobName = jobExecution.getJobInstance().getJobName();
//
//    if (jobExecution.getStatus().isUnsuccessful()) {
//      log.warn("CurationJobListener - 배치 작업 실패로 큐레이션 작업 건너뜀: {}", jobName);
//      return;
//    }
//
//    log.info("CurationJobListener - 배치 작업 완료 후 큐레이션 작업 시작: {}", jobName);
//
//    try {
//      // 각 배치 작업에 따라 다른 큐레이션 전략 적용
//      switch (jobName) {
//        case "tmdbJob":
//          handleTmdbJobCompletion(jobExecution);
//          break;
//        case "sportsJob":
//          handleSportsJobCompletion(jobExecution);
//          break;
//        default:
//          log.info("CurationJobListener - 알 수 없는 배치 작업: {}", jobName);
//      }
//    } catch (Exception e) {
//      log.error("CurationJobListener - 큐레이션 작업 실패: {}", jobName, e);
//    }
//  }
//
//  /**
//   * TMDB 배치 작업 완료 후 큐레이션 처리
//   */
//  private void handleTmdbJobCompletion(JobExecution jobExecution) {
//    log.info("handleTmdbJobCompletion - TMDB 큐레이션 작업 시작");
//
//    // Step 실행 정보에서 처리된 아이템 수 확인
//    long processedItems = jobExecution.getStepExecutions().stream()
//        .mapToLong(StepExecution::getWriteCount)
//        .sum();
//
//    log.info("handleTmdbJobCompletion - 처리된 TMDB 콘텐츠: {}개", processedItems);
//
//    if (processedItems > 0) {
//      // 최근에 추가된 TMDB 콘텐츠들을 조회하여 큐레이션 수행
//      List<Content> recentTmdbContents = contentRepository.findRecentTmdbContents();
//
//      if (!recentTmdbContents.isEmpty()) {
//        curationService.batchCurationForNewContents(recentTmdbContents);
//        log.info("handleTmdbJobCompletion - TMDB 큐레이션 완료: {}개 콘텐츠", recentTmdbContents.size());
//      }
//    }
//  }
//
//  /**
//   * Sports 배치 작업 완료 후 큐레이션 처리
//   */
//  private void handleSportsJobCompletion(JobExecution jobExecution) {
//    log.info("handleSportsJobCompletion - Sports 큐레이션 작업 시작");
//
//    long processedItems = jobExecution.getStepExecutions().stream()
//        .mapToLong(StepExecution::getWriteCount)
//        .sum();
//
//    log.info("handleSportsJobCompletion - 처리된 Sports 콘텐츠: {}개", processedItems);
//
//    if (processedItems > 0) {
//      // 최근에 추가된 Sports 콘텐츠들을 조회하여 큐레이션 수행
//      List<Content> recentSportsContents = contentRepository.findRecentSportsContents();
//
//      if (!recentSportsContents.isEmpty()) {
//        curationService.batchCurationForNewContents(recentSportsContents);
//        log.info("handleSportsJobCompletion - Sports 큐레이션 완료: {}개 콘텐츠", recentSportsContents.size());
//      }
//    }
//  }
}
