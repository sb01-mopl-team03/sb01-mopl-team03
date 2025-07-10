package team03.mopl.domain.curation.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.curation.entity.Keyword;

public interface CurationService {

  void init();

  Keyword registerKeyword(UUID userId, String keywordText);

  List<Content> curateContentForKeyword(Keyword keyword);

  List<ContentDto> getRecommendationsByKeyword(UUID keywordId, UUID userId);

  void batchCurationForNewContents(List<Content> newContents);

  void updateContentRating(UUID contentId);

  void delete(UUID keywordId, UUID userId);
}
