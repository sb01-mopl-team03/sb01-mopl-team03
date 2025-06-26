package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRoomRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DmRoomServiceImpl implements DmRoomService {
  private final DmRoomRepository dmRoomRepository;
  private final DmService dmService;

  @Override
  @Transactional
  public DmRoom createRoom(UUID senderId, UUID receiverId) {
    //추후 유저 검증 필요
    return dmRoomRepository.save(new DmRoom(senderId, receiverId));
  }

  @Override
  public DmRoom getRoom(UUID roomId) {
    return dmRoomRepository.findById(roomId).orElseThrow(()->new IllegalArgumentException("Room not found"));
  }

  @Override
  @Transactional
  public DmRoom findOrCreateRoom(UUID userA, UUID userB) {
    return dmRoomRepository.findByRoomBetweenUsers(userA, userB).orElseGet(()->dmRoomRepository.save(createRoom(userA, userB)));
  }

  // 유저의 모든 채팅방
  @Override
  public List<DmRoom> getAllRoomsForUser(UUID userId) {
    return dmRoomRepository.findBySenderIdOrReceiverId(userId, userId);
  }

  // 두 유저의 개인 채팅방 존재 여부
  @Override
  public boolean existsBetween(UUID userA, UUID userB) {
    return dmRoomRepository.findByRoomBetweenUsers(userA, userB).isPresent();
  }

  @Override
  public void deleteRoom(UUID roomId) {
    // 둘 다 나가기 전까지는 채팅이 살아있어야 함
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
    dmRoom.setSenderId(null);
    if( dmRoom.nobodyInRoom()){
      List<Dm> messages = dmRoom.getMessages();
      for (Dm message : messages) {
        dmService.deleteDm(message.getId());
      }
      dmRoomRepository.delete(dmRoom);
    }
  }

}
