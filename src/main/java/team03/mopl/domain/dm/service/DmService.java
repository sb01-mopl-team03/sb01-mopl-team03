package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.entity.Dm;

public interface DmService {

  DmDto sendDm(UUID senderId, UUID roomId, String content);

  List<DmDto> getDmList(UUID roomId, UUID userId);

  void readAll(UUID roomId, UUID userId);

  void deleteDm(UUID dmId);
}
