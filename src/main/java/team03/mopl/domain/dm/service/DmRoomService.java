package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.entity.DmRoom;

public interface DmRoomService {

  DmRoomDto createRoom(UUID senderId, UUID receiverId);

  DmRoomDto getRoom(UUID roomId);

  DmRoomDto findOrCreateRoom(UUID userA, UUID userB);

  List<DmRoomDto> getAllRoomsForUser(UUID userId);

  void deleteRoom(UUID userId, UUID roomId);

}
