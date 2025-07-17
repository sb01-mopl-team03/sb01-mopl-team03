package team03.mopl.domain.curation.dto;

import lombok.Builder;

@Builder
public record CursorPageRequest(
    String cursor,
    int size
) {
  public static CursorPageRequest of(String cursor, int size) {
    return CursorPageRequest.builder()
        .cursor(cursor)
        .size(Math.min(size, 50))
        .build();
  }

}
