package team03.mopl.domain.curation.service;

import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.curation.dto.CursorPageRequest;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.entity.Keyword;

public interface CurationService {

  void init();

  KeywordDto registerKeyword(UUID userId, String keywordText);

  // 메인 추천 조회 메서드
  @Transactional(readOnly = true)
  CursorPageResponseDto<ContentDto> getRecommendationsByKeyword(
      UUID keywordId,
      UUID userId,
      CursorPageRequest request
  );

  List<KeywordDto> getKeywordsByUser(UUID userId);

  void batchCurationForNewContents(List<Content> newContents);

  void updateContentRating(UUID contentId);

  void delete(UUID keywordId, UUID userId);
}
