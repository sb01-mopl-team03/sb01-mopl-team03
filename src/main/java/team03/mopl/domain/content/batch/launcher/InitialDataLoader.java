package team03.mopl.domain.content.batch.launcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.content.ContentType;

@Component
@Slf4j
public class InitialDataLoader implements ApplicationRunner {

  private final JobLauncher jobLauncher;
  @Qualifier("initialSportsJob")
  private final Job initialSportsJob;
  @Qualifier("initialTmdbJob")
  private final Job initialTmdbJob;
  private final ContentRepository contentRepository;

  public InitialDataLoader(
      JobLauncher jobLauncher,
      @Qualifier("initialSportsJob") Job initialSportsJob,
      @Qualifier("initialTmdbJob") Job initialTmdbJob,
      ContentRepository contentRepository) {
    this.jobLauncher = jobLauncher;
    this.initialSportsJob = initialSportsJob;
    this.initialTmdbJob = initialTmdbJob;
    this.contentRepository = contentRepository;
  }

  @Override
  public void run(ApplicationArguments args) throws Exception {

    try {
      if (!contentRepository.existsByContentType(ContentType.SPORTS)) {
        log.info("초기 스포츠 데이터 적재 시작");

        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("mode", "initial")
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(initialSportsJob, jobParameters);
        log.info("초기 스포츠 데이터 적재 프로세스 상태 : {}", jobExecution.getStatus());
      }
      if (!contentRepository.existsByContentType(ContentType.MOVIE)
          && !contentRepository.existsByContentType(ContentType.TV)) {
        log.info("초기 영화, TV(드라마) 데이터 적재 시작");

        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("timestamp", System.currentTimeMillis())
            .addString("mode", "initial")
            .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(initialTmdbJob, jobParameters);
        log.info("초기 영화, TV(드라마) 데이터 적재 프로세스 상태 : {}", jobExecution.getStatus());
      }
    } catch (Exception e) {
      log.error("배치 작업 실패", e);
    }
  }
}
