package team03.mopl.domain.watchroom.dto.participant;

import lombok.Builder;

@Builder
public record ParticipantDto(
    String username,
    String profile,
    boolean isOwner
) {
}
