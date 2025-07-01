package team03.mopl.domain.curation.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.curation.entity.Keyword;

public interface ContentRecommendService {

  void init();

  Keyword registerKeyword(UUID userId, String keywordText);

  List<Content> curateContentForKeyword(Keyword keyword);

  List<Content> getRecommendationsForUser(UUID userId, int limit);

  void batchCurationForNewContents(List<Content> newContents);

}
