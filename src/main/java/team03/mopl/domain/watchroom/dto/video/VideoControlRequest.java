package team03.mopl.domain.watchroom.dto.video;

import jakarta.annotation.Nullable;
import team03.mopl.domain.watchroom.entity.VideoControlAction;

public record VideoControlRequest(
    VideoControlAction videoControlAction,
    @Nullable
    Double currentTime
) {

}
