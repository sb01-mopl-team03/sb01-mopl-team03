package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.dm.DmRoomNotFoundException;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRepository;
import team03.mopl.domain.dm.repository.DmRoomRepository;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;

@Service
@RequiredArgsConstructor
public class DmServiceImpl implements DmService {
  private final DmRepository dmRepository;
  private final DmRoomRepository dmRoomRepository;
  private final NotificationService notificationService;

  @Override
  public DmDto sendDm(UUID senderId, UUID roomId, String content) {
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(DmRoomNotFoundException::new);

    Dm dm = new Dm(senderId, content);
    dm.setDmRoom(dmRoom); // 연관관계 설정

    // 알림 전송 추가
    UUID receiverId = dmRoom.getReceiverId();
    notificationService.sendNotification(receiverId, NotificationType.DM_RECEIVED, content);

    return DmDto.from(dmRepository.save(dm));
  }

  @Override
  @Transactional(readOnly = true)
  public List<DmDto> getDmList(UUID roomId, UUID userId) {
    readAll(roomId, userId); //dm 리스트를 가져온다는 건 모두 읽겠다는 뜻
    return dmRepository.findByDmRoomIdOrderByCreatedAtAsc(roomId).stream().map(DmDto::from).collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void readAll(UUID roomId, UUID userId) {
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("DM Room 없음"));
    List<Dm> messages = dmRoom.getMessages();
    for (Dm dm : messages) {
      //각 dm의 읽은 사람 목록에 없다면 포함시킴
      if(!dm.getReadUserIds().contains(userId)) {
        dm.readDm(userId);
      }
      /*if (!dm.isRead() && !dm.getSenderId().equals(userId)) {        dm.setRead();      }*/
    }
  }

  public void deleteDm(UUID dmId) {
    Dm dm = dmRepository.findById(dmId).orElseThrow(() -> new IllegalArgumentException("Dm을 찾을 수 없습니다."));
    dmRepository.delete(dm);
  }

}
