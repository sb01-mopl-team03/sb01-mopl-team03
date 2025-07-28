package team03.mopl.domain.curation;

import java.time.LocalDateTime;
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
import team03.mopl.domain.curation.service.ContentSearchService;
import team03.mopl.domain.curation.service.CurationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CurationJobListener implements JobExecutionListener {

  private final CurationService curationService;
  private final ContentRepository contentRepository;
  private final ContentSearchService contentSearchService;

  @BeforeJob
  public void beforeJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();
    log.info("CurationJobListener - 배치 작업 시작: {}", jobName);

    // 배치 시작 시간 기록
    jobExecution.getExecutionContext().put("batchStartTime", LocalDateTime.now());
  }

  @AfterJob
  public void afterJob(JobExecution jobExecution) {
    String jobName = jobExecution.getJobInstance().getJobName();

    if (jobExecution.getStatus().isUnsuccessful()) {
      log.warn("CurationJobListener - 배치 작업 실패로 큐레이션 작업 건너뜀: {}", jobName);
      return;
    }

    log.info("CurationJobListener - 배치 작업 완료 후 큐레이션 작업 시작: {}", jobName);

    try {
      LocalDateTime batchStartTime = (LocalDateTime) jobExecution.getExecutionContext()
          .get("batchStartTime");

      // 배치 작업에 따라 다른 큐레이션 전략 적용
      switch (jobName) {
        case "tmdbJob":
          handleTmdbJobCompletion(jobExecution, batchStartTime);
          break;
        case "sportsJob":
          handleSportsJobCompletion(jobExecution, batchStartTime);
          break;
      }
    } catch (Exception e) {
      log.error("CurationJobListener - 큐레이션 작업 실패: {}", jobName, e);
    }
  }

  /**
   * TMDB 배치 작업 완료 후 처리
   */
  private void handleTmdbJobCompletion(JobExecution jobExecution, LocalDateTime batchStartTime) {
    log.info("handleTmdbJobCompletion - TMDB 큐레이션 작업 시작");

    long processedItems = getProcessedItemCount(jobExecution);
    log.info("handleTmdbJobCompletion - 처리된 TMDB 콘텐츠: {}개", processedItems);

    if (processedItems > 0) {
      // 배치 시간 동안 생성된 TMDB 콘텐츠 조회
      List<Content> recentTmdbContents = contentRepository.findRecentTmdbContentsAfter(batchStartTime);

      if (!recentTmdbContents.isEmpty()) {
        // 1. OpenSearch 배치 인덱싱
        batchIndexContents(recentTmdbContents, "TMDB");

        // 2. 큐레이션 수행
        curationService.batchCurationForNewContents(recentTmdbContents);

        log.info("handleTmdbJobCompletion - TMDB 처리 완료: {}개 콘텐츠", recentTmdbContents.size());
      }
    }
  }

  /**
   * Sports 배치 작업 완료 후 처리
   */
  private void handleSportsJobCompletion(JobExecution jobExecution, LocalDateTime batchStartTime) {
    log.info("handleSportsJobCompletion - Sports 큐레이션 작업 시작");

    long processedItems = getProcessedItemCount(jobExecution);
    log.info("handleSportsJobCompletion - 처리된 Sports 콘텐츠: {}개", processedItems);

    if (processedItems > 0) {
      List<Content> recentSportsContents = contentRepository.findRecentSportsContentsAfter(batchStartTime);

      if (!recentSportsContents.isEmpty()) {
        batchIndexContents(recentSportsContents, "Sports");
        curationService.batchCurationForNewContents(recentSportsContents);

        log.info("handleSportsJobCompletion - Sports 처리 완료: {}개 콘텐츠", recentSportsContents.size());
      }
    }
  }

  /**
   * 배치에서 처리된 아이템 수 계산
   */
  private long getProcessedItemCount(JobExecution jobExecution) {
    return jobExecution.getStepExecutions().stream()
        .mapToLong(StepExecution::getWriteCount)
        .sum();
  }

  /**
   * 배치 인덱싱 수행
   */
  private void batchIndexContents(List<Content> contents, String jobType) {
    log.info("batchIndexContents - {} 배치 인덱싱 시작: {}개", jobType, contents.size());

    try {
      contentSearchService.batchIndexContents(contents);
      log.info("batchIndexContents - {} 배치 인덱싱 완료", jobType);
    } catch (Exception e) {
      log.error("batchIndexContents - {} 배치 인덱싱 실패", jobType, e);

      // 배치 인덱싱 실패 시 개별 인덱싱으로 폴백
      fallbackToIndividualIndexing(contents, jobType);
    }
  }

  /**
   * 개별 인덱싱 폴백
   */
  private void fallbackToIndividualIndexing(List<Content> contents, String jobType) {
    log.info("개별 인덱싱 폴백 시작 - {}: {}개", jobType, contents.size());

    int successCount = 0;
    int failCount = 0;

    for (Content content : contents) {
      try {
        contentSearchService.indexContent(content);
        successCount++;
      } catch (Exception e) {
        log.warn("개별 인덱싱 실패 - ID: {}, 제목: {}", content.getId(), content.getTitle());
        failCount++;
      }
    }

    log.info("개별 인덱싱 폴백 완료 - {}: 성공 {}개, 실패 {}개", jobType, successCount, failCount);
  }
}
