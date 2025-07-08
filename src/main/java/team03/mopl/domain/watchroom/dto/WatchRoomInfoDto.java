package team03.mopl.domain.watchroom.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Setter;

@Builder
public record WatchRoomInfoDto (
    UUID id,
    @Setter
    UUID newUserId,
    String contentTitle,
    ParticipantsInfoDto participantsInfoDto
){

}
