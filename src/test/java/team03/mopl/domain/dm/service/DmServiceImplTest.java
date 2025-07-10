package team03.mopl.domain.dm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team03.mopl.common.exception.dm.DmContentTooLongException;
import team03.mopl.common.exception.dm.DmNotFoundException;
import team03.mopl.common.exception.dm.DmRoomNotFoundException;
import team03.mopl.domain.dm.dto.SendDmDto;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRepository;
import team03.mopl.domain.dm.repository.DmRoomRepository;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
@ExtendWith(MockitoExtension.class)
class DmServiceImplTest {

  @Mock
  private DmRepository dmRepository;
  @Mock
  private DmRoomRepository dmRoomRepository;
  @Mock
  private NotificationService notificationService;

  @InjectMocks
  private DmServiceImpl dmService;

  private UUID senderId;
  private UUID receiverId;
  private UUID roomId;
  private DmRoom dmRoom;

  @BeforeEach
  void setUp() {
    senderId = UUID.randomUUID();
    receiverId = UUID.randomUUID();
    roomId = UUID.randomUUID();

    dmRoom = new DmRoom(roomId, senderId, receiverId);
  }

  @Test
  @DisplayName("DM 전송 - sendDm")
  void sendDm() {
    // given
    String content = "헬로 DM";

    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom));
    given(dmRepository.save(any(Dm.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    var result = dmService.sendDm(new SendDmDto(senderId, roomId, content));

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo(content);
    assertThat(result.getSenderId()).isEqualTo(senderId);

    then(notificationService).should().sendNotification(
        argThat(dto ->
            dto.getReceiverId().equals(dmRoom.getReceiverId())
                && dto.getNotificationType() == NotificationType.DM_RECEIVED
                && dto.getContent().equals(content)
        )
    );
  }

  @Test
  @DisplayName("roomId 에 해당하는 DmRoom 이 없을 때")
  void sendDm_shouldThrowDmRoomNotFoundException_whenRoomNotFound() {
    UUID senderId = UUID.randomUUID();
    UUID roomId = UUID.randomUUID();

    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThrows(DmRoomNotFoundException.class, () -> {
      dmService.sendDm(new SendDmDto(senderId, roomId, "hello"));
    });
  }

  @Test
  @DisplayName("255자를 초과하는 content 에러")
  void sendDm_shouldThrowContentTooLongException_whenContentExceeds255() {
    // given
    String tooLongContent = "a".repeat(256);

    assertThrows(DmContentTooLongException.class, () -> {
      dmService.sendDm(new SendDmDto(senderId, roomId, tooLongContent));
    });
  }


  @Test
  @DisplayName("DM 리스트 조회 - getDmList (모두 읽음 처리 포함)")
  void getDmList() {
    // given
    Dm dm1 = new Dm(senderId, "메시지1");
    dm1.setDmRoom(dmRoom);
    Dm dm2 = new Dm(senderId, "메시지2");
    dm2.setDmRoom(dmRoom);
    dmRoom.getMessages().addAll(List.of(dm1, dm2));

    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom));
    given(dmRepository.findByDmRoomIdOrderByCreatedAtAsc(roomId)).willReturn(List.of(dm1, dm2));

    // when
    var result = dmService.getDmList(roomId, receiverId);

    // then
    assertThat(result).hasSize(2);

    // 모든 메시지에 읽음 표시가 되었는지
    assertThat(dm1.getReadUserIds()).contains(receiverId);
    assertThat(dm2.getReadUserIds()).contains(receiverId);
  }

  @Test
  @DisplayName("모든 DM 읽음 처리 - readAll")
  void readAll() {
    // given
    Dm dm1 = new Dm(senderId, "안읽은 메시지");
    dmRoom.getMessages().add(dm1);

    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom));

    // when
    dmService.readAll(roomId, receiverId);

    // then
    assertThat(dm1.getReadUserIds()).contains(receiverId);
  }

  @Test
  @DisplayName("roomId 로 찾을 수 있는 DmRoom 이 없을 때")
  void readAll_shouldThrowDmRoomNotFoundException_whenRoomNotFound() {
    UUID roomId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThrows(DmRoomNotFoundException.class, () -> {
      dmService.readAll(roomId, userId);
    });
  }

  @Test
  @DisplayName("DM 삭제 - deleteDm")
  void deleteDm() {
    // given
    UUID dmId = UUID.randomUUID();
    Dm dm = new Dm(senderId, "삭제할 메시지");
    given(dmRepository.findById(dmId)).willReturn(Optional.of(dm));

    // when
    dmService.deleteDm(dmId);

    // then
    then(dmRepository).should().delete(dm);
  }

  @Test
  @DisplayName("dmId 에 해당하는 Dm 이 없을 때")
  void deleteDm_shouldThrowIllegalArgumentException_whenDmNotFound() {
    UUID dmId = UUID.randomUUID();

    when(dmRepository.findById(dmId)).thenReturn(Optional.empty());

    assertThrows(DmNotFoundException.class, () -> {
      dmService.deleteDm(dmId);
    });
  }
}
