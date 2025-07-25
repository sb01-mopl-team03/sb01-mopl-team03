package team03.mopl.domain.dm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.dm.DmDecodingError;
import team03.mopl.common.exception.dm.DmNotFoundException;
import team03.mopl.common.exception.dm.DmRoomNotFoundException;
import team03.mopl.common.exception.dm.NoOneMatchInDmRoomException;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmPagingDto;
import team03.mopl.domain.dm.dto.SendDmDto;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRepository;
import team03.mopl.domain.dm.repository.DmRepositoryCustom;
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
  private final ObjectMapper objectMapper;
  private final DmRepositoryCustom dmRepositoryCustom;

  @Override
  @Transactional
  public DmDto sendDm(SendDmDto sendDmDto) {
    log.info("sendDm - DM 전송 시도: senderId={}, roomId={}, content={}", sendDmDto.getSenderId(), sendDmDto.getRoomId(), sendDmDto.getContent());
    DmRoom dmRoom = dmRoomRepository.findById(sendDmDto.getRoomId()).orElseThrow(() -> {
      log.warn("sendDm - 존재하지 않는 방: roomId={}", sendDmDto.getRoomId());
      return new DmRoomNotFoundException();
    });

    Dm dm = new Dm(sendDmDto.getSenderId(), sendDmDto.getContent());
    dm.setDmRoom(dmRoom); // 연관관계 설정
    dm.readDm(sendDmDto.getSenderId());

    // 알림 전송 추가
    if (dmRoom.getSenderId().equals(sendDmDto.getSenderId())) {
      UUID receiverId = dmRoom.getReceiverId(); // dmRoom의 senderId 로 등록된 사람 == dm 받는 사람
      notificationService.sendNotification(new NotificationDto(receiverId, NotificationType.DM_RECEIVED, sendDmDto.getContent()));
    } else if (dmRoom.getReceiverId().equals(sendDmDto.getSenderId())) {
      UUID receiverId = dmRoom.getSenderId();
      notificationService.sendNotification(new NotificationDto(receiverId, NotificationType.DM_RECEIVED, sendDmDto.getContent()));
    } else {
      throw new NoOneMatchInDmRoomException();
    }

    Dm savedDm = dmRepository.save(dm);
    dmRoom.touchUpdatedAt(LocalDateTime.now());
    log.info("sendDm - DM 전송 완료: dmId={}", savedDm.getId());
    return DmDto.from(savedDm);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseDto<DmDto> getDmList(UUID roomId, DmPagingDto dmPagingDto, UUID userId) {
    log.info("getDmList - DM 목록 조회: roomId={}, userId={}", roomId, userId);

    String cursor = dmPagingDto.getCursor();
    Cursor decodeCursor;
    String mainCursorValue = null;
    String subCursorValue = null;
    if (cursor != null && !cursor.isEmpty()) {
      decodeCursor = decodeCursor(cursor);
      mainCursorValue = decodeCursor.lastValue();
      subCursorValue = decodeCursor.lastId();
    }
    int size = dmPagingDto.getSize();

    List<Dm> list = dmRepositoryCustom.findByCursor(roomId, size + 1, mainCursorValue, subCursorValue);
    long totalElements = dmRepository.count();
    boolean hasNext = list.size() > size; // 해당 DM이 마지막인지 확인

    //21개
    List<DmDto> dmDtoList = list.stream().map(DmDto::from).toList();

    String nextCursor = null;
    if (hasNext) {
      //더 보낼게 있는 것들
      nextCursor = dmDtoList.get(dmDtoList.size() - 1).getCreatedAt().toString(); //마지막 Dmdto
      dmDtoList.subList(0, size); //20개가 넘치니 자름
    }

    return CursorPageResponseDto.<DmDto>builder().data(dmDtoList).nextCursor(nextCursor).size(dmDtoList.size()).totalElements(totalElements)
        .hasNext(hasNext).build();
  }

  private Cursor decodeCursor(String base64) {
    try {
      String json = new String(Base64.getDecoder().decode(base64));
      return objectMapper.readValue(json, Cursor.class);
    } catch (Exception e) {
      log.warn("Base64 문자열을 디코딩하여 객체로 변환 중 오류 발생", e);
      throw new DmDecodingError();
    }
  }

  @Override
  @Transactional
  public void readAll(UUID roomId, UUID userId) {
    log.info("readAll - DM 모두 읽음 처리 시작: roomId={}, userId={}", roomId, userId);

    DmRoom dmRoom = dmRoomRepository.findById(roomId).orElseThrow(() -> {
      log.warn("readAll - 존재하지 않는 방: roomId={}", roomId);
      return new DmRoomNotFoundException();
    });
    int count = 0;
    List<Dm> messages = dmRoom.getMessages();
    for (Dm dm : messages) {
      //각 dm의 읽은 사람 목록에 없다면 포함시킴
      Set<UUID> alreadyReadUsers = new HashSet<>(dm.getReadUserIds());
      if (!alreadyReadUsers.contains(userId)) {
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
