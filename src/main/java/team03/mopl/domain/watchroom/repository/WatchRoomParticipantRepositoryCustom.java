package team03.mopl.domain.watchroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithHeadcountDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchInternalDto;

public interface WatchRoomParticipantRepositoryCustom {

  //will be deprecated
  List<WatchRoomContentWithHeadcountDto> getAllWatchRoomContentWithHeadcountDto();

  Long countWatchRoomContentWithHeadcountDto(String searchKeyword);

  List<WatchRoomContentWithHeadcountDto> getAllWatchRoomContentWithHeadcountDtoPaginated(
      WatchRoomSearchInternalDto request);

  Optional<WatchRoomContentWithHeadcountDto> getWatchRoomContentWithHeadcountDto(UUID id);

}
