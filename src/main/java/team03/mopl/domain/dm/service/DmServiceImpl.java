package team03.mopl.domain.dm.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRepository;
import team03.mopl.domain.dm.repository.DmRoomRepository;

@Service
@RequiredArgsConstructor
public class DmServiceImpl implements DmService {
  private final DmRepository dmRepository;
  private final DmRoomRepository dmRoomRepository;

  @Override
  public Dm sendDm(UUID senderId, UUID roomId, String content) {
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("DM 방을 찾을 수 없습니다."));

    Dm dm = new Dm(senderId, content);
    dm.setDmRoom(dmRoom); // 연관관계 설정

    return dmRepository.save(dm);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Dm> getDmList(UUID roomId) {
    return dmRepository.findByDmRoomIdOrderByCreatedAtAsc(roomId);
  }

  @Override
  @Transactional
  public void readAll(UUID roomId, UUID userId) {
    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> new IllegalArgumentException("DM Room 없음"));
    List<Dm> messages = dmRoom.getMessages();
    for (Dm dm : messages) {
      if (!dm.isRead() && !dm.getSenderId().equals(userId)) {
        dm.setRead();
      }
    }
  }

  public void deleteDm(UUID dmId) {
    Dm dm = dmRepository.findById(dmId).orElseThrow(() -> new IllegalArgumentException("Dm을 찾을 수 없습니다."));
    dmRepository.delete(dm);
  }

}
