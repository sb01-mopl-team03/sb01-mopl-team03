package team03.mopl.common.dto;

import java.util.List;
import lombok.Builder;

@Builder
public record CursorPageResponseDto<T>(
    List<T> data,
    String nextCursor,
    int size,
    long totalElements,
    boolean hasNext
) {
}