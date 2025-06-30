package team03.mopl.domain.admin;

import java.util.UUID;
import team03.mopl.domain.user.Role;

public record RoleUpdateRequest(
    UUID userId, Role newRole
) {

}
