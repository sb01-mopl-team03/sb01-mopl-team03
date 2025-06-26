package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.dm.entity.DmRoom;

public interface DmRoomService {

  DmRoom createRoom(UUID senderId, UUID receiverId);

  DmRoom getRoom(UUID roomId);

  DmRoom findOrCreateRoom(UUID userA, UUID userB);

  List<DmRoom> getAllRoomsForUser(UUID userId);

  boolean existsBetween(UUID userA, UUID userB);

  void deleteRoom(UUID roomId);

}
