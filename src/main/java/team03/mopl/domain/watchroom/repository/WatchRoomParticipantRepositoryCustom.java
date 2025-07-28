package team03.mopl.domain.watchroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithParticipantCountDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchInternalDto;

public interface WatchRoomParticipantRepositoryCustom {

  Long countWatchRoomContentWithHeadcountDto(String searchKeyword);

  List<WatchRoomContentWithParticipantCountDto> getAllWatchRoomContentWithHeadcountDtoPaginated(
      WatchRoomSearchInternalDto request);

  Optional<WatchRoomContentWithParticipantCountDto> getWatchRoomContentWithHeadcountDto(UUID id);

}
