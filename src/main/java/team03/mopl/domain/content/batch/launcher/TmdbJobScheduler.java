package team03.mopl.domain.content.batch.launcher;

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
public class TmdbJobScheduler {

  private final JobLauncher jobLauncher;
  private final Job tmdbJob;


  public TmdbJobScheduler(JobLauncher jobLauncher,
      @Qualifier("TmdbJob") Job tmdbJob) {
    this.jobLauncher = jobLauncher;
    this.tmdbJob = tmdbJob;
  }

    @Scheduled(cron = "0 0 5 ? * FRI")
  public void runTmdbJob() {
    JobParameters jobParameters = new JobParametersBuilder()
        .addLong("timestamp", System.currentTimeMillis())
        .addString("mode", "scheduler")
        .toJobParameters();

    try {
      log.info("Tmdb Job 스케줄러 시작");

      JobExecution jobExecution = jobLauncher.run(tmdbJob, jobParameters);

      log.info("배치 작업 완료 : {}", jobExecution.getStatus());

    } catch (Exception e) {
      log.error("Tmdb Job 실행 중 오류 발생", e);
    }
  }
}
