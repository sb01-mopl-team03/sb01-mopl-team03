package team03.mopl.domain.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 역할")
public enum Role {
  @Schema(description = "일반 사용자")
  ADMIN,

  @Schema(description = "관리자")
  USER
}
