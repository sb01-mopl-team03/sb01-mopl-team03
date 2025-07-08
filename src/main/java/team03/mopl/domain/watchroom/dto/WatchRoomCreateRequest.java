package team03.mopl.domain.watchroom.dto;

import java.util.UUID;

public record WatchRoomCreateRequest(
  UUID contentId,
  UUID ownerId
){
}
