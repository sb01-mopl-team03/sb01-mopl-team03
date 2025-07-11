package team03.mopl.domain.dm.service;

import java.util.UUID;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmPagingDto;
import team03.mopl.domain.dm.dto.SendDmDto;

public interface DmService {

  DmDto sendDm(SendDmDto sendDmDto);

  CursorPageResponseDto<DmDto> getDmList(UUID roomId, DmPagingDto dmPagingDto, UUID userId);

  void readAll(UUID roomId, UUID userId);

  void deleteDm(UUID dmId);
}
