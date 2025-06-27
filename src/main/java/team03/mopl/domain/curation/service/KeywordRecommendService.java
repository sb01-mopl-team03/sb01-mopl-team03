package team03.mopl.domain.curation.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.common.CursorPageResponse;
import team03.mopl.domain.content.Content;

public interface KeywordRecommendService {

  CursorPageResponse<Content> getKeywordRecommendations(UUID userId, String cursor, int size);

  CursorPageResponse<Content> searchByKeyword(String keyword, String cursor, int size);

  CursorPageResponse<Content> searchByMultipleKeywords(List<String> keywords, String cursor, int size);

  CursorPageResponse<Content> getSimilarContents(UUID contentId, String cursor, int size);

  CursorPageResponse<Content> getPopularContents(String cursor, int size);

  List<String> extractKeywordFromText(String text, int maxKeywords);

}
