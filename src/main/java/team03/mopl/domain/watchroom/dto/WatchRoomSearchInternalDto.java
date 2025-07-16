package team03.mopl.domain.watchroom.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import team03.mopl.common.dto.Cursor;

@Builder
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WatchRoomSearchInternalDto {

  private String searchKeyword;
  private Cursor cursor;
  private String direction;
  private String sortBy;
  private int size;

  public static WatchRoomSearchInternalDto fromRequestWithCursor(
      WatchRoomSearchDto watchRoomSearchDto, Cursor cursor) {
    return WatchRoomSearchInternalDto.builder()
        .searchKeyword(watchRoomSearchDto.getSearchKeyword())
        .direction(watchRoomSearchDto.getDirection())
        .cursor(cursor)
        .sortBy(watchRoomSearchDto.getSortBy())
        .size(watchRoomSearchDto.getSize() == null ? 20 : watchRoomSearchDto.getSize())
        .build();
  }
}