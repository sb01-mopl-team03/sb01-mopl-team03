package team03.mopl.domain.content.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import team03.mopl.domain.curation.CurationJobListener;

@Configuration
public class JobConfig {

  private final JobRepository jobRepository;
  private final CurationJobListener curationJobListener;

  @Qualifier("initialSportsStep")
  private final Step initialSportsStep;

  @Qualifier("sportsStep")
  private final Step sportsStep;

  @Qualifier("initialTmdbStep")
  private final Step initialTmdbStep;

  @Qualifier("tmdbStep")
  private final Step tmdbStep;


  public JobConfig(
      JobRepository jobRepository, CurationJobListener curationJobListener,
      @Qualifier("initialSportsStep") Step initialSportsStep,
      @Qualifier("sportsStep") Step sportsStep,
      @Qualifier("initialTmdbStep") Step initialTmdbStep,
      @Qualifier("tmdbStep") Step tmdbStep) {
    this.jobRepository = jobRepository;
    this.curationJobListener = curationJobListener;
    this.initialSportsStep = initialSportsStep;
    this.sportsStep = sportsStep;
    this.initialTmdbStep = initialTmdbStep;
    this.tmdbStep = tmdbStep;
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

  @Bean
  public Job initialTmdbJob(){
    return new JobBuilder("initialTmdbJob", jobRepository)
        .start(initialTmdbStep)
        .build();
  }

  @Bean
  public Job TmdbJob(){
    return new JobBuilder("TmdbJob", jobRepository)
        .start(tmdbStep)
        .build();
  }

  @Bean
  public Job sportCurationJob() {
    return new JobBuilder("sportCurationJob", jobRepository)
        .start(sportsStep)
        .listener(curationJobListener)
        .build();
  }

  @Bean
  public Job tmdbCurationJob() {
    return new JobBuilder("tmdbCurationJob", jobRepository)
        .start(tmdbStep)
        .listener(curationJobListener)
        .build();
  }
}
