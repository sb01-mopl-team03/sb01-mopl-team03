package team03.mopl.domain.watchroom.dto.participant;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ParticipantDto(
    UUID id,
    String username,
    String profile,
    boolean isOwner
) {
}
