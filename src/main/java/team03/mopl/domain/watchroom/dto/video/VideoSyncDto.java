package team03.mopl.domain.watchroom.dto.video;

import lombok.Builder;
import team03.mopl.domain.watchroom.entity.VideoControlAction;

@Builder
public record VideoSyncDto(
    VideoControlAction videoControlAction,
    Double currentTime,
    boolean isPlaying,
    long timestamp //서버가 발행한 시간
) {

}
