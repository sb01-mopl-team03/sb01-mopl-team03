package team03.mopl.domain.playlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PlaylistCreateRequest(
    @NotBlank(message = "플레이리스트 이름은 필수입니다")
    @Size(max = 100, message = "플레이리스트 이름은 100자 이하여야 합니다")
    String name,

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    String description,

    boolean isPublic
) {

}
