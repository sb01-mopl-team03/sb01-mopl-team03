package team03.mopl.domain.content.batch.launcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SportsJobScheduler {

  private final JobLauncher jobLauncher;
  private final Job sportsJob;

  public SportsJobScheduler(
      JobLauncher jobLauncher,
      @Qualifier("sportsJob") Job sportsJob) {
    this.jobLauncher = jobLauncher;
    this.sportsJob = sportsJob;
  }

  @Scheduled(cron = "0 22 23 * * *")
  public void runSportsJob() {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .addString("mode", "scheduler")
        .toJobParameters();

    try {
      log.info("Sports Job 스케줄 실행 시작");

      JobExecution jobExecution = jobLauncher.run(sportsJob, jobParameters);
      log.info("배치 작업 완료 : {}", jobExecution.getStatus());

    } catch (Exception e) {
      log.error("Sports Job 실행 중 오류 발생", e);
    }
  }
}
