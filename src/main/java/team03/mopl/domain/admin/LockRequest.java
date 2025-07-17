package team03.mopl.domain.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "사용자 계정 잠금 또는 잠금 해제 요청 DTO")
public record LockRequest(
    @Schema(description = "잠금 또는 해제할 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId) {
}
