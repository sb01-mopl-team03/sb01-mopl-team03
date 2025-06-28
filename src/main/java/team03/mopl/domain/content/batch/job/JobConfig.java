package team03.mopl.domain.content.batch.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@RequiredArgsConstructor
public class JobConfig {

  private final JobRepository jobRepository;
  private final Step initialSportsStep;

  @Bean
  public Job initialSportsJob(){
    return new JobBuilder("initialSportsJob", jobRepository)
        .start(initialSportsStep)
        .build();
  }
}
