package team03.mopl.domain.curation.dto;

import java.util.UUID;

public record KeywordResponse(
    UUID userId,
    String keyword
) {

}
