package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.domain.dm.dto.DmRoomDto;
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
  public DmRoomDto createRoom(UUID senderId, UUID receiverId) {
    //추후 유저 검증 필요
    return DmRoomDto.from(dmRoomRepository.save(new DmRoom(senderId, receiverId)));
  }

  @Override
  public DmRoomDto getRoom(UUID roomId) {
    return DmRoomDto.from(dmRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found")));
  }

  @Override
  @Transactional
  public DmRoomDto findOrCreateRoom(UUID userA, UUID userB) {
    return dmRoomRepository.findByRoomBetweenUsers(userA, userB).map(DmRoomDto::from).orElseGet(() -> createRoom(userA, userB));
  }

  // 유저의 모든 채팅방
  @Override
  public List<DmRoomDto> getAllRoomsForUser(UUID userId) {
    return dmRoomRepository.findBySenderIdOrReceiverId(userId, userId).stream().map(DmRoomDto::from).collect(Collectors.toList());
  }
  @Override
  public void deleteRoom(UUID userId, UUID roomId) { //파라미터 변경! ( 삭제하고자 하는 룸의 유저가 삭제가능 )
    // 둘 다 나가기 전까지는 채팅이 살아있어야 함
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
    if( dmRoom.getReceiverId().equals(userId) || dmRoom.getSenderId().equals(userId)) {
      //둘 중 하나의 유저가 나가고 싶어함
      if (dmRoom.getSenderId().equals(userId)){
        dmRoom.setSenderId(null);
      }else {
        dmRoom.setReceiverId(null);
      }
      //아무도 없는지 확인
      if (dmRoom.nobodyInRoom()) {
        List<Dm> messages = dmRoom.getMessages();
        for (Dm message : messages) {
          dmService.deleteDm(message.getId());
        }
        dmRoomRepository.delete(dmRoom);
      }
    }
  }

}
