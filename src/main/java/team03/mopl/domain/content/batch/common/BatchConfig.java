package team03.mopl.domain.content.batch.common;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentRepository;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {

  private final ContentRepository contentRepository;

  @Bean
  public ItemWriter<Content> itemWriter(){
    return new ApiWriter(contentRepository);
  }
}
