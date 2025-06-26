package team03.mopl.domain.chat.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageCreateRequest(

    UUID chatRoomId,
    UUID userId,
    String content,
    LocalDateTime createdAt

) {

}
