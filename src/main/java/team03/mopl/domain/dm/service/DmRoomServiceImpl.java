package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.dm.DmRoomNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRoomRepository;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DmRoomServiceImpl implements DmRoomService {

  private final DmRoomRepository dmRoomRepository;
  private final DmService dmService;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public DmRoomDto createRoom(UUID senderId, UUID receiverId) {
    log.info("createRoom - DM 방 생성 요청: senderId={}, receiverId={}", senderId, receiverId);
    //추후 유저 검증 필요
    userRepository.findById(senderId).orElseThrow(() -> {
      log.warn("createRoom - 유저(senderId) 없음: {}", senderId);
      return new UserNotFoundException();
    });
    userRepository.findById(receiverId).orElseThrow(() -> {
      log.warn("createRoom - 유저(receiverId) 없음: {}", receiverId);
      return new UserNotFoundException();
    });
    DmRoomDto roomDto = DmRoomDto.from(dmRoomRepository.save(new DmRoom(senderId, receiverId)));
    log.info("createRoom - DM 방 생성 완료: roomId={}", roomDto.getId());

    // 알림 전송 추가
    notificationService.sendNotification(new NotificationDto(receiverId, NotificationType.NEW_DM_ROOM, "새로운 DM 방이 생성되었습니다."));

    return roomDto;
  }

  @Override
  public DmRoomDto getRoom(UUID roomId) {
    log.info("getRoom - roomId={}", roomId);
    return DmRoomDto.from(
        dmRoomRepository.findById(roomId).orElseThrow(() -> {
          log.warn("getRoom - 존재하지 않는 roomId={}", roomId);
          return new DmRoomNotFoundException();
        })
    );
  }

  @Override
  @Transactional
  public DmRoomDto findOrCreateRoom(UUID userA, UUID userB) {
    return dmRoomRepository.findByRoomBetweenUsers(userA, userB).map(DmRoomDto::from).orElseGet(() -> createRoom(userA, userB));
  }

  // 유저의 모든 채팅방
  @Override
  public List<DmRoomDto> getAllRoomsForUser(UUID userId) {
    //내가 sender 이거나 receiver 이거나
    return dmRoomRepository.findBySenderIdOrReceiverId(userId, userId).stream()
        .map(d -> DmRoomDto.from(d.getMessages().get(d.getMessages().size() - 1).getContent(), getUnreadCount(d.getId(), userId), d)).toList();
  }

  public int getUnreadCount(UUID roomId, UUID userId) {
    DmRoom dmRoom = dmRoomRepository.findById(roomId)
        .orElseThrow(DmRoomNotFoundException::new);
    return (int) dmRoom.getMessages()
        .stream()
        .filter(dm -> !dm.getReadUserIds().contains(userId))
        .count();
  }

  @Override
  public void deleteRoom(UUID userId, UUID roomId) { //파라미터 변경! ( 삭제하고자 하는 룸의 유저가 삭제가능 )
    // 둘 다 나가기 전까지는 채팅이 살아있어야 함
    log.info("deleteRoom - 유저 {} 가 방 {} 삭제 시도", userId, roomId);
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> {
      log.warn("deleteRoom - 방 없음: {}", roomId);
      return new DmRoomNotFoundException();
    });

    if (dmRoom.getSenderId() != null && dmRoom.getSenderId().equals(userId)) {
      log.info("deleteRoom - senderId 제거: {}", userId);
      dmRoom.setSenderId(null);
    } else if (dmRoom.getReceiverId() != null && dmRoom.getReceiverId().equals(userId)) {
      log.info("deleteRoom - receiverId 제거: {}", userId);
      dmRoom.setReceiverId(null);
    }

    if (dmRoom.nobodyInRoom()) {
      log.info("deleteRoom - 아무도 남지 않아 방과 메시지 전체 삭제: roomId={}", roomId);
      List<Dm> messages = dmRoom.getMessages();
      for (Dm message : messages) {
        log.info("deleteRoom - 메시지 삭제: messageId={}", message.getId());
        dmService.deleteDm(message.getId());
      }
      dmRoomRepository.delete(dmRoom);
    }
  }

}
