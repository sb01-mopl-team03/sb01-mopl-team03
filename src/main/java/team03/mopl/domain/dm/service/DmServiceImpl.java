package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.dm.DmNotFoundException;
import team03.mopl.common.exception.dm.DmRoomNotFoundException;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.SendDmDto;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRepository;
import team03.mopl.domain.dm.repository.DmRoomRepository;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class DmServiceImpl implements DmService {
  private final DmRepository dmRepository;
  private final DmRoomRepository dmRoomRepository;
  private final NotificationService notificationService;

  @Override
  public DmDto sendDm(SendDmDto sendDmDto) {
    log.info("sendDm - DM 전송 시도: senderId={}, roomId={}, content={}", sendDmDto.getSenderId(), sendDmDto.getRoomId(), sendDmDto.getContent());
    DmRoom dmRoom = dmRoomRepository.findById(sendDmDto.getRoomId()).orElseThrow(() -> {
      log.warn("sendDm - 존재하지 않는 방: roomId={}", sendDmDto.getRoomId());
      return new DmRoomNotFoundException();
    });

    Dm dm = new Dm(sendDmDto.getSenderId(),  sendDmDto.getContent());
    dm.setDmRoom(dmRoom); // 연관관계 설정

    // 알림 전송 추가
    UUID receiverId = dmRoom.getReceiverId();
    notificationService.sendNotification(new NotificationDto(receiverId, NotificationType.DM_RECEIVED,  sendDmDto.getContent()));

    Dm savedDm = dmRepository.save(dm);
    log.info("sendDm - DM 전송 완료: dmId={}", savedDm.getId());
    return DmDto.from(savedDm);
  }

  @Override
  @Transactional(readOnly = true)
  public List<DmDto> getDmList(UUID roomId, UUID userId) {
    log.info("getDmList - DM 목록 조회: roomId={}, userId={}", roomId, userId);
    readAll(roomId, userId); //dm 리스트를 가져온다는 건 모두 읽겠다는 뜻
    return dmRepository.findByDmRoomIdOrderByCreatedAtAsc(roomId).stream().map(DmDto::from).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void readAll(UUID roomId, UUID userId) {
    log.info("readAll - DM 모두 읽음 처리 시작: roomId={}, userId={}", roomId, userId);

    DmRoom dmRoom = dmRoomRepository.findById(roomId)
        .orElseThrow(() -> {
          log.warn("readAll - 존재하지 않는 방: roomId={}", roomId);
          return new DmRoomNotFoundException();
        });
    int count = 0;
    List<Dm> messages = dmRoom.getMessages();
    for (Dm dm : messages) {
      //각 dm의 읽은 사람 목록에 없다면 포함시킴
      if (!dm.getReadUserIds().contains(userId)) {
        dm.readDm(userId);
        count++;
      }
    }
    log.info("readAll - 읽음 처리된 메시지 수: {}", count);
  }

  public void deleteDm(UUID dmId) {
    log.info("deleteDm - DM 삭제 시도: dmId={}", dmId);

    Dm dm = dmRepository.findById(dmId).orElseThrow(() -> {
      log.warn("deleteDm - 존재하지 않는 DM: dmId={}", dmId);
      return new DmNotFoundException();
    });
    dmRepository.delete(dm);
    log.info("deleteDm - DM 삭제 완료: dmId={}", dmId);

  }

}
