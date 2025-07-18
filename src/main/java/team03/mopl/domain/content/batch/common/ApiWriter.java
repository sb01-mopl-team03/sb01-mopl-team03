package team03.mopl.domain.content.batch.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;

@Slf4j
@RequiredArgsConstructor
public class ApiWriter implements ItemWriter<Content> {

  private final ContentRepository contentRepository;

  @Override
  public void write(Chunk<? extends Content> chunk) throws Exception {
    contentRepository.saveAll(chunk.getItems());
    log.info("ApiWriter - Item {}개 저장", chunk.getItems().size());
  }
}
