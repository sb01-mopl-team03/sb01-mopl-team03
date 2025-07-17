package team03.mopl.domain.watchroom.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.watchroom.entity.WatchRoom;


public record WatchRoomDto(

    UUID id,
    String title,
    String contentTitle,
    UUID ownerId,
    String ownerName,
    LocalDateTime createdAt,
    Long headCount

) {

  public static WatchRoomDto fromWatchRoomWithHeadcount(WatchRoom watchRoom, long headcount ) {
    return new WatchRoomDto(
        watchRoom.getId(),
        watchRoom.getTitle(),
        watchRoom.getContent().getTitle(),
        watchRoom.getOwner().getId(),
        watchRoom.getOwner().getName(),
        watchRoom.getCreatedAt(),
        headcount
    );
  }

  public static WatchRoomDto from(WatchRoomContentWithHeadcountDto watchRoomContentWithHeadcountDto) {
    return new WatchRoomDto(
        watchRoomContentWithHeadcountDto.getWatchRoom().getId(),
        watchRoomContentWithHeadcountDto.getWatchRoom().getTitle(),
        watchRoomContentWithHeadcountDto.getContent().getTitle(),
        watchRoomContentWithHeadcountDto.getWatchRoom().getOwner().getId(),
        watchRoomContentWithHeadcountDto.getWatchRoom().getOwner().getName(),
        watchRoomContentWithHeadcountDto.getWatchRoom().getCreatedAt(),
        watchRoomContentWithHeadcountDto.getHeadCount()
    );
  }

}
