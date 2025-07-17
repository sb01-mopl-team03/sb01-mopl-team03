package team03.mopl.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "플레이리스트에 콘텐츠 추가 요청")
public record AddContentsRequest (

    @Schema(description = "추가할 콘텐츠 ID 목록", example = "[\"uuid-1\", \"uuid-2\"]")
    @NotNull(message = "음악 ID 목록은 필수입니다")
    @Size(min = 1, message = "최소 1개의 콘텐츠 ID가 필요합니다")
    List<UUID> contentIds
) {

}