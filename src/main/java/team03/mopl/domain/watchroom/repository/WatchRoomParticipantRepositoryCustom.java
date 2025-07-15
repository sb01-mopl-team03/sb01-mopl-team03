package team03.mopl.domain.watchroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithHeadcountDto;

public interface WatchRoomParticipantRepositoryCustom {

  List<WatchRoomContentWithHeadcountDto> getAllWatchRoomContentWithHeadcountDto();

  Optional<WatchRoomContentWithHeadcountDto> getWatchRoomContentWithHeadcountDto(UUID id);

}
