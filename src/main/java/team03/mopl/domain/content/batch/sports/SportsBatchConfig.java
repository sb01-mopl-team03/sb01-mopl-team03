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
import team03.mopl.domain.content.ContentRepository;

@Configuration
@RequiredArgsConstructor
public class SportsBatchConfig {

  private final RestTemplate restTemplate;
  private final PlatformTransactionManager transactionManager;
  private final ContentRepository contentRepository;
  private final JobRepository jobRepository;

  @Value("${sports.baseurl}")
  private String baseUrl;

  @Bean
  public Step initialSportsStep(){
    return new StepBuilder("initialSportsStep", jobRepository)
        .<SportsItemDto, Content>chunk(20, transactionManager)
        .reader(initialSportsReader())
        .processor(initialSportsProcessor())
        .writer(initialSportsWriter())
        .build();
  }

  @Bean
  public ItemReader<SportsItemDto> initialSportsReader(){
    return new InitialSportsApiReader(restTemplate, baseUrl);
  }

  @Bean
  public ItemProcessor<SportsItemDto, Content> initialSportsProcessor(){
    return new InitialSportsApiProcessor();
  }

  @Bean
  public ItemWriter<Content> initialSportsWriter(){
    return new InitialSportsApiWriter(contentRepository);
  }
}
