package team03.mopl.domain.content.batch.tmdb;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;
import team03.mopl.domain.content.Content;

@Configuration
@RequiredArgsConstructor
public class TmdbBatchConfig {

  private final RestTemplate restTemplate;
  private final JobRepository jobRepository;
  private final PlatformTransactionManager transactionManager;
  private final ItemWriter<Content> itemWriter;

  @Value("${tmdb.baseurl}")
  private String baseurl;
  @Value("${tmdb.api_token}")
  private String apiToken;

  @Bean
  public Step initialTmdbStep(){
    return new StepBuilder("initialTmdbStep", jobRepository)
        .<TmdbItemDto, Content>chunk(20, transactionManager)
        .reader(initialTmdbReader())
        .processor(tmdbProcessor())
        .writer(itemWriter)
        .build();
  }

  @Bean
  public ItemStreamReader<TmdbItemDto> initialTmdbReader(){
    return new InitialTmdbApiReader(restTemplate, baseurl, apiToken);
  }

  @Bean
  public ItemProcessor<TmdbItemDto, Content> tmdbProcessor(){
    return new InitialTmdbApiProcessor(restTemplate, baseurl, apiToken);
  }
}
