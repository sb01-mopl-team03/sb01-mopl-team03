package team03.mopl.domain.dm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.dm.DmContentTooLongException;
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
  @Mock
  private DmRepositoryCustom dmRepositoryCustom;

  @InjectMocks
  private DmServiceImpl dmService;

  private UUID userA;
  private UUID userB;
  private UUID roomId;
  private DmRoom dmRoom_SenderEqualDmRoomSenderUserA;
  private DmRoom dmRoom_SenderEqualDmRoomReceiverUserB;


  @BeforeEach
  void setUp() {
    userA = UUID.randomUUID();
    userB = UUID.randomUUID();
    roomId = UUID.randomUUID();

    dmRoom_SenderEqualDmRoomSenderUserA = new DmRoom(roomId, userA, userB);
    dmRoom_SenderEqualDmRoomReceiverUserB = new DmRoom(roomId, userA, userB);
    ReflectionTestUtils.setField(dmService, "objectMapper", new ObjectMapper());
  }

  @Test
  @DisplayName("DM 전송 - sendDm")
  void sendDm_SenderEqualDmRoomSender() {
    // given
    String content = "헬로 DM - DM 보내는 사람이 DM ROOM의 Sender일 때";

    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom_SenderEqualDmRoomSenderUserA));
    given(dmRepository.save(any(Dm.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    var result = dmService.sendDm(new SendDmDto(userA, roomId, content));

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo(content);
    assertThat(result.getSenderId()).isEqualTo(userA);

    then(notificationService).should().sendNotification(
        argThat(dto ->
            dto.getReceiverId().equals(dmRoom_SenderEqualDmRoomSenderUserA.getReceiverId())
                && dto.getNotificationType() == NotificationType.DM_RECEIVED
                && dto.getContent().equals(content)
        )
    );
  }
  @Test
  @DisplayName("DM 전송 - sendDm - DM 보내는 사람이 DM ROOM의 Receiver일 때")
  void sendDm_SenderEqualDmRoomReceiver() {
    // given
    String content = "헬로 DM";

    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom_SenderEqualDmRoomReceiverUserB));
    given(dmRepository.save(any(Dm.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    var result = dmService.sendDm(new SendDmDto(userB, roomId, content));

    // then
    assertThat(result).isNotNull();
    assertThat(result.getContent()).isEqualTo(content);
    assertThat(result.getSenderId()).isEqualTo(userB);

    then(notificationService).should().sendNotification(
        argThat(dto ->
            dto.getReceiverId().equals(dmRoom_SenderEqualDmRoomReceiverUserB.getSenderId())
                && dto.getNotificationType() == NotificationType.DM_RECEIVED
                && dto.getContent().equals(content)
        )
    );
  }
  @Test
  @DisplayName("DM 전송 - sendDm - NoOneMatchInDmRoomException")
  void sendDm_NoOneMatchInDmRoomException() {
    // given
    UUID invalidSenderId = UUID.randomUUID(); // sender도 receiver도 아님
    String content = "헬로 DM";

    // 실제로 senderId, receiverId와 다르게 구성된 방
    DmRoom dmRoom = new DmRoom(userA, userB);
    ReflectionTestUtils.setField(dmRoom, "id", roomId);

    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom));

    // when & then
    assertThrows(NoOneMatchInDmRoomException.class, () -> {
      dmService.sendDm(new SendDmDto(invalidSenderId, roomId, content));
    });
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
      dmService.sendDm(new SendDmDto(userA, roomId, tooLongContent));
    });
  }


  @Test
  @DisplayName("DM 리스트 조회 - getDmList (모두 읽음 처리 포함)")
  void getDmList() {
    // given
    Dm dm1 = new Dm(userA, "메시지1");
    dm1.setDmRoom(dmRoom_SenderEqualDmRoomSenderUserA);
    Dm dm2 = new Dm(userA, "메시지2");
    dm2.setDmRoom(dmRoom_SenderEqualDmRoomSenderUserA);
    dm1.readDm(userA);
    dm2.readDm(userA);
    dmRoom_SenderEqualDmRoomSenderUserA.getMessages().addAll(List.of(dm1, dm2));

    // 커서 조회 결과는 dm1, dm2
    given(dmRepositoryCustom.findByCursor(eq(roomId), anyInt(), any(), any()))
        .willReturn(List.of(dm1, dm2));
    given(dmRepository.count()).willReturn(2L);

    // when
    var pagingDto = new DmPagingDto(null, 20); // cursor 없음, size=20
    var result = dmService.getDmList(roomId, pagingDto, userB);

    // then
    assertThat(result.data()).hasSize(2);
    assertThat(result.hasNext()).isFalse();
    assertThat(dm1.getReadUserIds()).contains(userA);
    assertThat(dm2.getReadUserIds()).contains(userA);
  }

  @Test
  @DisplayName("DM 리스트 조회 - hasNext가 true일 때")
  void getDmList_hasNext() {
    // given
    List<Dm> list = new ArrayList<>();
    for(int i=0; i<40; i++){
      Dm dm = new Dm(userA, "메시지"+i);
      dm.setDmRoom(dmRoom_SenderEqualDmRoomSenderUserA);
      dm.readDm(userA);
      list.add(dm);
    }
    dmRoom_SenderEqualDmRoomSenderUserA.getMessages().addAll(list);

    // 커서 조회 결과는 dm1, dm2
    given(dmRepositoryCustom.findByCursor(eq(roomId), anyInt(), any(), any()))
        .willReturn(list.subList(0, 21));
    given(dmRepository.count()).willReturn(20L);

    // when
    var pagingDto = new DmPagingDto(null, 20); // cursor 없음, size=20
    var result = dmService.getDmList(roomId, pagingDto, userB);

    // then
    assertThat(result.data()).hasSize(21);
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("getDmList - 커서가 있을 때")
  void getDmList_withCursor() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    Cursor fakeCursor = new Cursor("2025-07-21T00:00", UUID.randomUUID().toString());

    String encodedCursor = encodeCursor(fakeCursor);
    DmPagingDto pagingDto = new DmPagingDto(encodedCursor, 20);

    // findByCursor에서 아무거나 리턴해도 상관없음
    given(dmRepositoryCustom.findByCursor(any(), anyInt(), any(), any()))
        .willReturn(Collections.emptyList());

    // when
    CursorPageResponseDto<DmDto> result = dmService.getDmList(roomId, pagingDto, userId);

    // then
    assertThat(result).isNotNull();
    verify(dmRepositoryCustom).findByCursor(eq(roomId), eq(21), eq(fakeCursor.lastValue()), eq(fakeCursor.lastId()));
  }

  @Test
  @DisplayName("getDmList - decodeError(.JsonParseException)")
  void getDmList_decodeError_JsonParseException() throws Exception {
    //decodeError 발생 시킴
    String encodedCursor = Base64.getUrlEncoder().encodeToString(new byte[1]);
    DmPagingDto pagingDto = new DmPagingDto(encodedCursor, 20);

    // then
    assertThrows(DmDecodingError.class, () -> dmService.getDmList(roomId, pagingDto, userA));
  }
  @Test
  @DisplayName("getDmList - decodeError 발생 시 DmDecodingError(Illegal_base64)")
  void getDmList_decodeError_Illegal_base64() {
    // given
    UUID userId = UUID.randomUUID();
    String invalidBase64 = "%%%INVALID_BASE64%%%"; // 명백히 Base64 인코딩 형식 아님
    DmPagingDto pagingDto = new DmPagingDto(invalidBase64, 20);

    // when & then
    assertThrows(DmDecodingError.class, () -> dmService.getDmList(roomId, pagingDto, userId));
  }



  @Test
  @DisplayName("readAll_roomId 로 찾을 수 있는 DmRoom 이 없을 때")
  void readAll_shouldThrowDmRoomNotFoundException_whenRoomNotFound() {
    UUID roomId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThrows(DmRoomNotFoundException.class, () -> {
      dmService.readAll(roomId, userId);
    });
  }

  @Test
  @DisplayName("readAll_정상 작동")
  void readAll() {
    // given
    List<Dm> list = new ArrayList<>();
    for(int i=0; i<40; i++){
      Dm dm = new Dm(userA, "메시지"+i);
      dm.setDmRoom(dmRoom_SenderEqualDmRoomSenderUserA);
      dm.readDm(userA);
      list.add(dm);
    }
    dmRoom_SenderEqualDmRoomSenderUserA.getMessages().addAll(list);
    for (Dm dm : list) {
      assertThat(dm.getReadUserIds()).hasSize(1);
    }

    // 커서 조회 결과는 dm1, dm2
    given(dmRoomRepository.findById(roomId))
        .willReturn(Optional.ofNullable(dmRoom_SenderEqualDmRoomSenderUserA));
    dmRoom_SenderEqualDmRoomSenderUserA.getMessages().addAll(list);
    // when
    dmService.readAll(roomId, userB);

    // then
    for (Dm dm : list) {
      assertThat(dm.getReadUserIds()).hasSize(2);
    }
  }

  @Test
  @DisplayName("DM 삭제 - deleteDm")
  void deleteDm() {
    // given
    UUID dmId = UUID.randomUUID();
    Dm dm = new Dm(userA, "삭제할 메시지");
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

  private String encodeCursor(Cursor cursor) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = objectMapper.writeValueAsString(cursor);
    return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }

}
