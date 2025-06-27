package team03.mopl.domain.content.batch.common;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

  @Bean
  public RestTemplate restTemplate(){
    return new RestTemplate();
  }
}
