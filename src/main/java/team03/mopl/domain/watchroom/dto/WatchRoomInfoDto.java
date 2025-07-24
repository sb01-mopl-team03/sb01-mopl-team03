package team03.mopl.domain.watchroom.dto;

import java.util.UUID;
import lombok.Builder;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.watchroom.dto.participant.ParticipantsInfoDto;

@Builder
public record WatchRoomInfoDto (
    UUID id,
    String title,
    UUID newUserId,
    Double playTime,
    boolean isPlaying,
    ParticipantsInfoDto participantsInfoDto,
    ContentDto content){

}
