package team03.mopl.domain.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import team03.mopl.domain.user.Role;

@Schema(description = "사용자 권한 변경 요청 DTO")
public record RoleUpdateRequest(
    @Schema(description = "권한을 변경할 사용자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,

    @Schema(description = "새로운 역할 (예: USER, ADMIN)", example = "ADMIN")
    Role newRole
) {

}
