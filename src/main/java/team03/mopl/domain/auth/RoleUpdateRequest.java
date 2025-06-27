package team03.mopl.domain.auth;

import java.util.UUID;
import team03.mopl.domain.user.Role;

public record RoleUpdateRequest(
    UUID userId, Role newRole
) {

}
