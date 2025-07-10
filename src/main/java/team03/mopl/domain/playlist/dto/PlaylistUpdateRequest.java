package team03.mopl.domain.playlist.dto;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PlaylistUpdateRequest(
    @Size(max = 100, message = "플레이리스트 이름은 100자 이하여야 합니다")
    String name,

    Boolean isPublic,

    List<UUID> contentIds

    ) {

}
