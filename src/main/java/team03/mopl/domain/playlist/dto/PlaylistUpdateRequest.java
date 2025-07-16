package team03.mopl.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PlaylistUpdateRequest(
    @Schema(description = "수정할 플레이리스트 이름", example = "출근길 재즈")
    @Size(max = 100, message = "플레이리스트 이름은 100자 이하여야 합니다")
    String name,

    @Schema(description = "공개 여부", example = "false")
    Boolean isPublic
    ) {

}
