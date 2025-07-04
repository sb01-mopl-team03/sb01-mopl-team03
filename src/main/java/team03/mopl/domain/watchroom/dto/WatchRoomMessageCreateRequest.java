package team03.mopl.domain.watchroom.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WatchRoomMessageCreateRequest(

    UUID chatRoomId,
    UUID userId,
    String content,
    LocalDateTime createdAt

) {

}
