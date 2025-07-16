package team03.mopl.domain.watchroom.dto;

import java.util.UUID;

//todo - @AuthenticationPrincipal 적용으로 수정
public record WatchRoomCreateRequest(
  UUID contentId,
  UUID ownerId,
  String title
){
}
