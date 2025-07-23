package team03.mopl.domain.dm.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.dm.AlreadyDmRoomExistException;
import team03.mopl.common.exception.dm.CannotCreateDmRoomSelfException;
import team03.mopl.common.exception.dm.DmRoomNotFoundException;
import team03.mopl.common.exception.dm.OutUserFromDmRoomException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRoomRepository;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.User;
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
    User sender = userRepository.findById(senderId).orElseThrow(() -> {
      log.warn("createRoom - 유저(senderId) 없음: {}", senderId);
      return new UserNotFoundException();
    });
    User receiver = userRepository.findById(receiverId).orElseThrow(() -> {
      log.warn("createRoom - 유저(receiverId) 없음: {}", receiverId);
      return new UserNotFoundException();
    });

    dmRoomRepository.findByRoomBetweenUsers(senderId, receiverId).ifPresent(room -> {
      if (room.isOut(senderId) || room.isOut(receiverId)) {
        throw new OutUserFromDmRoomException();
      }
      throw new AlreadyDmRoomExistException();
    });

    DmRoom dmRoom = dmRoomRepository.save(new DmRoom(senderId, receiverId));
    DmRoomDto roomDto = DmRoomDto.from(sender.getName(), receiver.getName(), dmRoom);
    log.info("createRoom - DM 방 생성 완료: roomId={}", roomDto.getId());

    // 알림 전송 추가
    notificationService.sendNotification(new NotificationDto(receiverId, NotificationType.NEW_DM_ROOM, "새로운 DM 방이 생성되었습니다."));

    return roomDto;
  }

  @Override
  public DmRoomDto getRoom(UUID roomId) {
    log.info("getRoom - roomId={}", roomId);
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> {
      log.warn("getRoom - 존재하지 않는 roomId={}", roomId);
      return new DmRoomNotFoundException();
    });
    User sender = userRepository.findById(dmRoom.getSenderId()).orElseThrow(UserNotFoundException::new);
    User receiver = userRepository.findById(dmRoom.getReceiverId()).orElseThrow(UserNotFoundException::new);
    return DmRoomDto.from(sender.getName(), receiver.getName(), dmRoom);
  }

  @Override
  @Transactional
  public DmRoomDto findOrCreateRoom(UUID userA, UUID userB) {
    User sender = userRepository.findById(userA).orElseThrow(UserNotFoundException::new);
    User receiver = userRepository.findById(userB).orElseThrow(UserNotFoundException::new);
    if( userA.toString().equals(userB.toString()) ) {
      log.warn("findOrCreateRoom - 자기 자신 {} 과의 채팅방을 생성 시도", userA);
      throw new CannotCreateDmRoomSelfException();
    }
    Optional<DmRoom> optional = dmRoomRepository.findByRoomBetweenUsers(userA, userB);
    if (optional.isPresent()) {
      DmRoom room = optional.get();
      // 둘 중 하나라도 outUsers에 포함되어 있으면?
      if (room.isOut(userA) || room.isOut(userB)) {
        throw new OutUserFromDmRoomException();
      }
      return DmRoomDto.from(sender.getName(), receiver.getName(), room);
    }
    // 기존 방이 없을 때만 생성
    return createRoom(userA, userB);
  }

  // 유저의 모든 채팅방
  @Override
  public List<DmRoomDto> getAllRoomsForUser(UUID userId) {
    List<DmRoom> dmRooms = dmRoomRepository.findBySenderIdOrReceiverId(userId, userId);
    List<DmRoomDto> dmRoomDtos = new ArrayList<>();
    for (DmRoom dmRoom : dmRooms) {
      if( dmRoom.isOut(userId) ) {
        continue;
      }
      List<Dm> messages = dmRoom.getMessages();
      String content = null;
      if (!messages.isEmpty()) {
        content = messages.get(messages.size() - 1).getContent();
      }
      int unreadCount = getUnreadCount(dmRoom.getId(), userId);
      User sender = userRepository.findById(dmRoom.getSenderId()).orElseThrow(UserNotFoundException::new);
      User receiver = userRepository.findById(dmRoom.getReceiverId()).orElseThrow(UserNotFoundException::new);

      dmRoomDtos.add(DmRoomDto.from(content, unreadCount, sender.getName(), receiver.getName(), dmRoom));
    }
    return dmRoomDtos;
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
  @Transactional
  public void reenterRoom(UUID userId, UUID roomId) {
    log.info("reenterRoom - roomId={}, userId={}", roomId, userId);
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> {
      log.warn("reenterRoom - 방 없음: {}", roomId);
      return new DmRoomNotFoundException();
    });
    dmRoom.removeOutUser(userId);
  }

  @Override
  @Transactional
  public void deleteRoom(UUID userId, UUID roomId) { //파라미터 변경! ( 삭제하고자 하는 룸의 유저가 삭제가능 )
    // 둘 다 나가기 전까지는 채팅이 살아있어야 함
    log.info("deleteRoom - 유저 {} 가 방 {} 삭제 시도", userId, roomId);
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> {
      log.warn("deleteRoom - 방 없음: {}", roomId);
      return new DmRoomNotFoundException();
    });

    //한 쪽 유저가 삭제된 경우
    UUID senderId   = dmRoom.getSenderId();
    UUID receiverId = dmRoom.getReceiverId();
    if (senderId != null && !userRepository.existsById(senderId)) {
      log.info("deleteRoom - 유저 {} 가 존재하지 않아 방 {} 에서 나감 처리", senderId, roomId);
      dmRoom.addOutUser(senderId);
    }
    if (receiverId != null && !userRepository.existsById(receiverId)) {
      log.info("deleteRoom - 유저 {} 가 존재하지 않아 방 {} 에서 나감 처리", receiverId, roomId);
      dmRoom.addOutUser(receiverId);
    }

    //senderId, ReceiverId 모두 남아있음 <- 한 명은 나가있는데 그 한 명이 다시 채팅방 똑같은 걸 생성하는 걸 방지해야 함
    if (dmRoom.getSenderId() != null && dmRoom.getSenderId().equals(userId)) {
      log.info("deleteRoom - senderId 제거: {}", userId);
      //dmRoom.setSenderId(null);
      dmRoom.addOutUser(userId);
      System.out.println("dmRoom = " + dmRoom.getOutUsers());
      dmRoomRepository.save(dmRoom);
    } else if (dmRoom.getReceiverId() != null && dmRoom.getReceiverId().equals(userId)) {
      log.info("deleteRoom - receiverId 제거: {}", userId);
      //dmRoom.setReceiverId(null);
      dmRoom.addOutUser(userId);
      System.out.println("dmRoom = " + dmRoom.getOutUsers());
      dmRoomRepository.save(dmRoom);
    }

    //나간 유저 리스트에 송수신자가 모두 포함된다면 방과 메시지 삭제 로직 실행

    Set<UUID> outUsers = dmRoom.getOutUsers();
    System.out.println("outUsers = " + outUsers);
    if( outUsers.contains(dmRoom.getSenderId()) && outUsers.contains(dmRoom.getReceiverId()) ) {
      log.info("deleteRoom - 아무도 남지 않아 방과 메시지 전체 삭제: roomId={}", roomId);
      List<Dm> messages = dmRoom.getMessages();
      for (Dm message : messages) {
        log.info("deleteRoom - 메시지 삭제: messageId={}", message.getId());
        dmService.deleteDm(message.getId());
        System.out.println("DmRoomServiceImpl.deleteRoom");
      }
      dmRoomRepository.delete(dmRoom);
    }
    /*if (dmRoom.nobodyInRoom()) {
      log.info("deleteRoom - 아무도 남지 않아 방과 메시지 전체 삭제: roomId={}", roomId);
      List<Dm> messages = dmRoom.getMessages();
      for (Dm message : messages) {
        log.info("deleteRoom - 메시지 삭제: messageId={}", message.getId());
        dmService.deleteDm(message.getId());
      }
      dmRoomRepository.delete(dmRoom);
    }*/
  }

}
