package team03.mopl.domain.watchroom.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.content.Content;

@AllArgsConstructor
@Getter
@Setter
public class WatchRoomContentWithParticipantCountDto {

  private WatchRoom watchRoom;
  private Content content;
  Long participantCount;

}
