package team03.mopl.domain.watchroom.dto;

import java.util.UUID;
import team03.mopl.domain.watchroom.entity.WatchRoom;


public record WatchRoomDto(

    UUID id,
    String contentTitle,
    UUID ownerId,
    Long headCount

) {

  public static WatchRoomDto fromWatchRoomWithHeadcount(WatchRoom watchRoom, long headcount ) {
    return new WatchRoomDto(
        watchRoom.getId(),
        watchRoom.getContent().getTitle(),
        watchRoom.getOwnerId(),
        headcount
    );
  }

  public static WatchRoomDto from(WatchRoomContentWithHeadcountDto watchRoomContentWithHeadcountDto) {
    return new WatchRoomDto(
        watchRoomContentWithHeadcountDto.getWatchRoom().getId(),
        watchRoomContentWithHeadcountDto.getContent().getTitle(),
        watchRoomContentWithHeadcountDto.getWatchRoom().getOwnerId(),
        watchRoomContentWithHeadcountDto.getHeadCount()
    );
  }

}
