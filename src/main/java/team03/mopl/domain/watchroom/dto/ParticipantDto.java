package team03.mopl.domain.watchroom.dto;

import lombok.Builder;

@Builder
public record ParticipantDto(
    String username,
    String profile,
    boolean isOwner
) {
}
