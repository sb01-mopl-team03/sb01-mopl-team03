package team03.mopl.domain.playlist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "플레이리스트 생성 요청")
public record PlaylistCreateRequest(

    @Schema(description = "플레이리스트 이름", example = "내가 좋아하는 음악들")
    @NotBlank(message = "플레이리스트 이름은 필수입니다")
    @Size(max = 100, message = "플레이리스트 이름은 100자 이하여야 합니다")
    String name,

    @Schema(description = "플레이리스트 설명", example = "감성 넘치는 플레이리스트")
    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    String description,

    @Schema(description = "공개 여부", example = "true")
    boolean isPublic
) {}
