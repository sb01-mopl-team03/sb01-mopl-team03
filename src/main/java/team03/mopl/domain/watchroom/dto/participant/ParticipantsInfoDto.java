package team03.mopl.domain.watchroom.dto.participant;

import java.util.List;
import lombok.Builder;

@Builder
public record ParticipantsInfoDto(

    List<ParticipantDto> participantDtoList,
    int participantsCount

) {


}
