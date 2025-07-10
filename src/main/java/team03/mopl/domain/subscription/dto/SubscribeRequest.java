package team03.mopl.domain.subscription.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubscribeRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    UUID userId,

    @NotNull(message = "플레이리스트 ID는 필수입니다")
    UUID playlistId
) {
}
