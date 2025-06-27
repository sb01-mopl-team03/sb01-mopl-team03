package team03.mopl.domain.chat.dto;

import java.util.UUID;

public record ChatRoomCreateRequest (
  UUID contentId,
  UUID ownerId
){
}
