package team03.mopl.domain.playlist.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record AddContentsRequest (
    @NotNull(message = "음악 ID 목록은 필수입니다")
    @Size(min = 1, message = "최소 1개의 음악 ID가 필요합니다")
    List<UUID> contentIds
) {

}