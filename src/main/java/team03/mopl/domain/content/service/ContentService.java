package team03.mopl.domain.content.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.content.dto.ContentDto;

public interface ContentService {

  List<ContentDto> getAll();

  void updateContentRating(UUID contentId);
}
