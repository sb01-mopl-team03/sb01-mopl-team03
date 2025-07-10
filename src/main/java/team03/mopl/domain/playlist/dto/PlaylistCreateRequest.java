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

    @NotNull(message = "플레이리스트 생성자는 필수입니다")
    UUID userId,

    boolean isPublic,

    List<UUID> contentIds
) {

}
