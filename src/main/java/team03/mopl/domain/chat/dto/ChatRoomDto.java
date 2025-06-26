package team03.mopl.domain.chat.dto;

import java.util.UUID;

public record ChatRoomDto(

    UUID id,
    UUID contentId,
    Integer headCount

) {

}
