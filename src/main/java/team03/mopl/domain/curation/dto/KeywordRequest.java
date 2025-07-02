package team03.mopl.domain.curation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record KeywordRequest(
    @NotNull(message = "사용자 ID는 필수입니다")
    UUID userId,

    @NotBlank(message = "키워드는 필수입니다")
    @Size(min = 1, max = 100, message = "키워드는 1자 이상 100자 이히여야 합니다")
    String keyword
) {

}
