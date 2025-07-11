package team03.mopl.domain.content.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;

public interface ContentService {

  List<ContentDto> getAll();

  CursorPageResponseDto<ContentDto> getCursorPage(ContentSearchRequest contentSearchRequest);

  ContentDto getContent(UUID id);

  void updateContentRating(UUID contentId);
}
