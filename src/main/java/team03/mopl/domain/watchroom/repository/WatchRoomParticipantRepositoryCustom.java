package team03.mopl.domain.watchroom.repository;

import java.util.List;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithHeadcountDto;

public interface WatchRoomParticipantRepositoryCustom {

  List<WatchRoomContentWithHeadcountDto> getAllWatchRoomContentWithHeadcountDto();

}
