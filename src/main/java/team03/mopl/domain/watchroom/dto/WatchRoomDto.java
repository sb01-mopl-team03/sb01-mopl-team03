package team03.mopl.domain.watchroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import team03.mopl.domain.watchroom.entity.WatchRoom;

@Schema(description = "실시간 같이 보기 DTO")
public record WatchRoomDto(


    @Schema(description = "실시간 같이 보기  ID", example = "1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d")
    UUID id,

    String title,
  
    @Schema(description = "컨텐츠 제목", example = "기생충")
    String contentTitle,

    @Schema(description = "실시간 같이 보기 생성자 ID", example = "a1b2c3d4-1234-5678-9012-abcdefabcdef")
    UUID ownerId,

    @Schema(description = "실시간 같이 보기 생성자 이름", example = "홍길동")
    String ownerName,

    @Schema(description = "실시간 같이 보기 생성 시각", example = "2025-07-16T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "실시간 같이 보기 참여 인원 수", example = "5")
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

  public static WatchRoomDto from(
      WatchRoomContentWithParticipantCountDto watchRoomContentWithParticipantCountDto) {
    return new WatchRoomDto(
        watchRoomContentWithParticipantCountDto.getWatchRoom().getId(),
        watchRoomContentWithParticipantCountDto.getWatchRoom().getTitle(),
        watchRoomContentWithParticipantCountDto.getContent().getTitle(),
        watchRoomContentWithParticipantCountDto.getWatchRoom().getOwner().getId(),
        watchRoomContentWithParticipantCountDto.getWatchRoom().getOwner().getName(),
        watchRoomContentWithParticipantCountDto.getWatchRoom().getCreatedAt(),
        watchRoomContentWithParticipantCountDto.getParticipantCount()
    );
  }

}
