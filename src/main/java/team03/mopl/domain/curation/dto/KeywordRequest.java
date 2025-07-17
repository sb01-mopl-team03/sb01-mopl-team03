package team03.mopl.domain.curation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "키워드 등록 요청 DTO")
public record KeywordRequest(

    @Schema(description = "사용자 ID", example = "abc123ef-4567-89ab-cdef-0123456789ab", required = true)
    @NotNull(message = "사용자 ID는 필수입니다")
    UUID userId,

    @Schema(description = "키워드", example = "다큐멘터리", required = true)
    @NotBlank(message = "키워드는 필수입니다")
    @Size(min = 1, max = 100, message = "키워드는 1자 이상 100자 이히여야 합니다")
    String keyword

) {
}
