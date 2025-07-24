package team03.mopl.domain.content.batch.sports;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.CurationJobListener;

@Configuration
@RequiredArgsConstructor
public class SportsBatchConfig {

  private final ContentRepository contentRepository;
  private final RestTemplate restTemplate;
  private final PlatformTransactionManager transactionManager;
  private final ItemWriter<Content> itemWriter;
  private final JobRepository jobRepository;
  private final CurationJobListener curationJobListener;

  @Value("${sports.baseurl}")
  private String baseUrl;

  @Bean
  public Step initialSportsStep(){
    return new StepBuilder("initialSportsStep", jobRepository)
        .<SportsItemDto, Content>chunk(20, transactionManager)
        .reader(initialSportsReader())
        .processor(sportsProcessor())
        .writer(itemWriter)
        .build();
  }

  @Bean
  public Step sportsStep(){
    return new StepBuilder("sportsStep", jobRepository)
        .<SportsItemDto, Content>chunk(5, transactionManager)
        .reader(sportsReader())
        .processor(sportsProcessor())
        .writer(itemWriter)
        .build();
  }

  @Bean
  public ItemReader<SportsItemDto> initialSportsReader(){
    return new InitialSportsApiReader(restTemplate, baseUrl);
  }

  @Bean
  public ItemReader<SportsItemDto> sportsReader(){
    return new SportsApiReader(restTemplate, baseUrl);
  }

  @Bean
  public ItemProcessor<SportsItemDto, Content> sportsProcessor(){
    return new SportsApiProcessor(contentRepository);
  }
}
