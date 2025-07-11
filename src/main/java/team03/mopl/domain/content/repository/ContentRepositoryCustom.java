package team03.mopl.domain.content.repository;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.content.Content;

public interface ContentRepositoryCustom {

  List<Content> findContentsWithCursor(
      String title,
      String contentType,
      String sortBy,
      String direction,
      String cursor,
      UUID cursorId,
      int size);
}
