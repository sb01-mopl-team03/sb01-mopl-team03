package team03.mopl.domain.content.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class JobConfig {

  private final JobRepository jobRepository;

  @Qualifier("initialSportsStep")
  private final Step initialSportsStep;

  @Qualifier("sportsStep")
  private final Step sportsStep;

  public JobConfig(
      JobRepository jobRepository,
      @Qualifier("initialSportsStep") Step initialSportsStep,
      @Qualifier("sportsStep") Step sportsStep) {
    this.jobRepository = jobRepository;
    this.initialSportsStep = initialSportsStep;
    this.sportsStep = sportsStep;
  }

  @Bean
  public Job initialSportsJob(){
    return new JobBuilder("initialSportsJob", jobRepository)
        .start(initialSportsStep)
        .build();
  }

  @Bean
  public Job sportsJob(){
    return new JobBuilder("sportsJob", jobRepository)
        .start(sportsStep)
        .build();
  }
}
