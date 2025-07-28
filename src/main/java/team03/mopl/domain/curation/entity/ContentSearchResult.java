package team03.mopl.domain.curation.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import team03.mopl.domain.content.Content;

@Getter
@AllArgsConstructor
public class ContentSearchResult {

  private final Content content;

  private final double score;

  @Override
  public String toString() {
    return String.format("ContentSearchResult{contentId=%s, title='%s', score=%.3f}",
        content.getId(), content.getTitle(), score);
  }
}
