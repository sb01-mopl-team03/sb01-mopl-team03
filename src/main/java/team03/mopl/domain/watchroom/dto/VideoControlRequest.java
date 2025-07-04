package team03.mopl.domain.watchroom.dto;

import team03.mopl.domain.watchroom.entity.VideoControlAction;

public record VideoControlRequest(
    VideoControlAction videoControlAction,
    Double currentTime
) {

}
