package team03.mopl.domain.content.service;

import java.util.UUID;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;

public interface ContentService {

  CursorPageResponseDto<ContentDto> getCursorPage(ContentSearchRequest contentSearchRequest);

  ContentDto getContent(UUID id);

  void updateContentRating(UUID contentId);
}
