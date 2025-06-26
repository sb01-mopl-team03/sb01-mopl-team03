package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.dm.entity.Dm;

public interface DmService {

  Dm sendDm(UUID senderId, UUID roomId, String content);

  List<Dm> getDmList(UUID roomId);

  void readAll(UUID roomId, UUID userId);

  void deleteDm(UUID dmId);
}
