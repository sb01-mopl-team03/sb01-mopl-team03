package team03.mopl.domain.watchroom.dto;

import java.util.UUID;
import lombok.Builder;

@Builder
public record WatchRoomInfoDto (
    UUID id,
    UUID newUserId,
    String contentTitle,
    ParticipantsInfoDto participantsInfoDto
){

}
