package team03.mopl.domain.curation.entity;

import lombok.Getter;
import team03.mopl.domain.content.Content;

@Getter
public class ContentWithScore {
  private Content content;
  private Double score;
  private String cursor;

  public ContentWithScore(Content content, Double score) {
    this.content = content;
    this.score = score;
    this.cursor = String.format("%.6_%s", score, content.getId().toString());
  }

}
