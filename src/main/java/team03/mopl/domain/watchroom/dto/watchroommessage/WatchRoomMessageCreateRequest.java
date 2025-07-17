package team03.mopl.domain.watchroom.dto.watchroommessage;

import java.time.LocalDateTime;
import java.util.UUID;

public record WatchRoomMessageCreateRequest(

    UUID chatRoomId,
    String content,
    LocalDateTime createdAt

) {

}
