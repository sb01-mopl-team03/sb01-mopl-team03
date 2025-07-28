package team03.mopl.domain.dm.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import team03.mopl.common.exception.dm.DmRoomNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.repository.DmRoomRepository;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class DmRoomServiceImplTest {

  @Mock
  private DmRoomRepository dmRoomRepository;
  @Mock
  private DmService dmService;
  @Mock
  private UserRepository userRepository;
  @Mock
  private NotificationService notificationService;
  @Mock
  private PasswordEncoder passwordEncoder;
  @Spy
  @InjectMocks
  private DmRoomServiceImpl dmRoomService;
  @InjectMocks
  private UserServiceImpl userService;

  private UUID senderId;
  private UUID receiverId;
  private User sender;
  private User receiver;

  @BeforeEach
  void setUp() {
    senderId = UUID.randomUUID();
    receiverId = UUID.randomUUID();

    sender = User.builder()
        .id(senderId)
        .email("sender@test.com")
        .name("sender")
        .password("pw")
        .build();

    receiver = User.builder()
        .id(receiverId)
        .email("receiver@test.com")
        .name("receiver")
        .password("pw")
        .build();
  }

  @Test
  @DisplayName("DM Room 생성 - 정상 동작")
  void createRoom() {
    // given
    given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
    given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));

    //첫번째 인자 즉 dmRoom을 그대로 반환하는 로직으로 계획
    given(dmRoomRepository.save(any(DmRoom.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    var result = dmRoomService.createRoom(senderId, receiverId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSenderId()).isEqualTo(senderId);
    assertThat(result.getReceiverId()).isEqualTo(receiverId);

    //notificationService.sendNotification() 메서드가 적절한 인자를 가지고 호출되었는 지 확인
    then(notificationService).should().sendNotification(
        argThat(dto ->
            dto.getReceiverId().equals(receiverId)
                && dto.getNotificationType() == NotificationType.NEW_DM_ROOM
                && dto.getContent() != null
        )
    );
  }

  @Test
  @DisplayName("DM Room 생성 - 에러 체크")
  void createRoom_shouldThrowException_whenSenderNotFound() {
    //sender 로직
    UUID senderId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    when(userRepository.findById(senderId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      dmRoomService.createRoom(senderId, receiverId);
    });
    //receiver 로직
    when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
    when(userRepository.findById(receiverId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      dmRoomService.createRoom(senderId, receiverId);
    });
  }


  @Test
  @DisplayName("Dm Room 조회")
  void getRoom() {
    // given
    UUID roomId = UUID.randomUUID();
    DmRoom dmRoom = new DmRoom(roomId, senderId, receiverId);

    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom));
    given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
    given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
    // when
    var result = dmRoomService.getRoom(roomId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getSenderId()).isEqualTo(senderId);
    assertThat(result.getReceiverId()).isEqualTo(receiverId);
    assertThat(result.getId()).isEqualTo(roomId);
  }

  @Test
  @DisplayName("Dm Room 조회 - 예외 체크")
  void getRoom_shouldThrowDmRoomNotFoundException_whenRoomNotFound() {
    UUID roomId = UUID.randomUUID();

    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.empty());

    assertThrows(DmRoomNotFoundException.class, () -> {
      dmRoomService.getRoom(roomId);
    });
  }

  @Test
  @DisplayName("findOrCreateRoom - 기존 방이 있으면 그 방을 반환한다")
  void findOrCreateRoom_existingRoom() {
    // given
    UUID roomId = UUID.randomUUID();
    DmRoom existingRoom = new DmRoom(roomId, senderId, receiverId);

    given(dmRoomRepository.findByRoomBetweenUsers(senderId, receiverId))
        .willReturn(Optional.of(existingRoom));
    when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
    when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
    // when
    var result = dmRoomService.findOrCreateRoom(senderId, receiverId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(roomId);
    assertThat(result.getSenderId()).isEqualTo(senderId);
    assertThat(result.getReceiverId()).isEqualTo(receiverId);
  }

  @Test
  @DisplayName("findOrCreateRoom - 기존 방이 없으면 새로 생성하고 반환한다")
  void findOrCreateRoom_createNewRoom() {
    // given
    UUID newRoomId = UUID.randomUUID();

    given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
    given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));

    given(dmRoomRepository.findByRoomBetweenUsers(senderId, receiverId))
        .willReturn(Optional.empty());

    given(dmRoomRepository.save(any(DmRoom.class)))
        .willAnswer(invocation -> {
          DmRoom arg = invocation.getArgument(0);
          return new DmRoom(newRoomId, arg.getSenderId(), arg.getReceiverId());
        });

    // when
    var result = dmRoomService.findOrCreateRoom(senderId, receiverId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(newRoomId);
    assertThat(result.getSenderId()).isEqualTo(senderId);
    assertThat(result.getReceiverId()).isEqualTo(receiverId);

    then(notificationService).should().sendNotification(
        argThat(dto ->
            dto.getReceiverId().equals(receiverId)
                && dto.getNotificationType() == NotificationType.NEW_DM_ROOM
                && dto.getContent() != null // 메시지가 null 이 아님만 확인
        )
    );

  }

  @Test
  @DisplayName("findOrCreateRoom - 유저가 존재하지 않는 경우 예외 체크")
  void findOrCreateRoom_shouldThrowUserNotFoundException_whenNoUsersExist() {
    UUID userA = UUID.randomUUID();
    UUID userB = UUID.randomUUID();
    //userA가 존재하지 않을 때
    when(userRepository.findById(userA)).thenReturn(Optional.empty());
    assertThrows(UserNotFoundException.class, () -> {
      dmRoomService.findOrCreateRoom(userA, userB);
    });

    //UserB가 존재하지 않을 때
    when(userRepository.findById(userA)).thenReturn(Optional.of(sender));
    when(userRepository.findById(userB)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      dmRoomService.findOrCreateRoom(userA, userB);
    });
  }


  @Test
  @DisplayName("getAllRoomsForUser - 유저가 속한 모든 DM Room을 조회한다")
  void getAllRoomsForUser() {
    // given
    UUID roomId1 = UUID.randomUUID();
    UUID roomId2 = UUID.randomUUID();
    UUID senderId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();

    // 메시지 생성
    Dm message1 = new Dm(senderId, "안녕1");
    Dm message2 = new Dm(senderId, "안녕2");

    // 메시지 읽음 설정
    message1.getReadUserIds().add(receiverId);
    // message2는 읽은 사람 없음

    // 방
    DmRoom room1 = new DmRoom(roomId1, senderId, receiverId);
    room1.getMessages().add(message1);
    ReflectionTestUtils.setField(room1, "lastMessageAt", LocalDateTime.now().minusMinutes(1));
    DmRoom room2 = new DmRoom(roomId2, senderId, receiverId);
    room2.getMessages().add(message2);
    ReflectionTestUtils.setField(room2, "lastMessageAt", LocalDateTime.now());
    // Repository Stubbing
    given(dmRoomRepository.findBySenderIdOrReceiverId(senderId, senderId))
        .willReturn(List.of(room1, room2));
    given(userRepository.findById(senderId)).willReturn(Optional.of(sender));
    given(userRepository.findById(receiverId)).willReturn(Optional.of(receiver));
    given(dmRoomRepository.findById(roomId1)).willReturn(Optional.of(room1));
    given(dmRoomRepository.findById(roomId2)).willReturn(Optional.of(room2));

    // when
    List<DmRoomDto> results = dmRoomService.getAllRoomsForUser(senderId);

    // then
    assertThat(results).hasSize(2);

    DmRoomDto dto1 = results.stream().filter(d -> d.getId().equals(roomId1)).findFirst().orElseThrow();
    DmRoomDto dto2 = results.stream().filter(d -> d.getId().equals(roomId2)).findFirst().orElseThrow();

    assertThat(dto1.getLastMessage()).isEqualTo("안녕1");
    assertThat(dto2.getLastMessage()).isEqualTo("안녕2");

    assertThat(dto1.getNewMessageCount()).isEqualTo(1L);
    assertThat(dto2.getNewMessageCount()).isEqualTo(1L);
  }

  @Test
  @DisplayName("sender 먼저 나가고 receiver는 남아있어 방 유지")
  void deleteSender () {
    // given
    UUID roomId = UUID.randomUUID();
    DmRoom dmRoom = new DmRoom(roomId, senderId, receiverId);
    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.of(dmRoom));
    when(userRepository.existsById(senderId)).thenReturn(true);
    when(userRepository.existsById(receiverId)).thenReturn(true);

    // when
    dmRoomService.deleteRoom(senderId, roomId);

    // then
    assertTrue(dmRoom.getOutUsers().contains(senderId));
    assertFalse(dmRoom.getOutUsers().contains(receiverId));
    verify(dmRoomRepository, never()).delete(any());
    verify(dmService, never()).deleteDm(any());
    // 변경 flush/save 확인
    verify(dmRoomRepository).save(dmRoom);
  }

  @Test
      @DisplayName("둘다_나가면_방과_DM_모두_삭제된다")
  void NobodyInRoom() {
    // GIVEN
    Dm dm1 = mock(Dm.class);
    UUID dm1Id = UUID.randomUUID();
    when(dm1.getId()).thenReturn(dm1Id);

    Dm dm2 = mock(Dm.class);
    UUID dm2Id = UUID.randomUUID();
    when(dm2.getId()).thenReturn(dm2Id);

    // 실제 엔티티에 메시지 추가
    UUID roomId = UUID.randomUUID();
    DmRoom dmRoom = new DmRoom(roomId, senderId, receiverId);
    ReflectionTestUtils.setField(dmRoom, "id", roomId); // id만 세팅
    dmRoom.getMessages().add(dm1);
    dmRoom.getMessages().add(dm2);

    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.of(dmRoom));
    when(userRepository.existsById(any())).thenReturn(true);

    // WHEN: sender 먼저 나감 → receiver 나감
    dmRoomService.deleteRoom(senderId, roomId);
    dmRoomService.deleteRoom(receiverId, roomId);

    // THEN: 메시지 삭제 로직이 호출되어야 함
    verify(dmService).deleteDm(dm1Id);
    verify(dmService).deleteDm(dm2Id);
    verify(dmRoomRepository).delete(dmRoom);
  }
  @Test
  @DisplayName("방없음_=>_예외")
  void CannotFindRoom() {
    UUID roomId = UUID.randomUUID();
    DmRoom dmRoom = new DmRoom(roomId, senderId, receiverId);
    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.empty());
    assertThrows(DmRoomNotFoundException.class,
        () -> dmRoomService.deleteRoom(senderId, roomId));
  }
  @Test
  @DisplayName("sender가_DB에존재하지않으면_outUsers에_추가된다")
  void DoesntExistSenderWillPutInOutUsers() {
    // given
    UUID roomId = UUID.randomUUID();
    DmRoom dmRoom = new DmRoom(roomId, senderId, receiverId);
    when(dmRoomRepository.findById(roomId)).thenReturn(Optional.of(dmRoom));
    when(userRepository.existsById(senderId)).thenReturn(false); // 존재 X
    when(userRepository.existsById(receiverId)).thenReturn(true);

    // when
    dmRoomService.deleteRoom(receiverId, roomId);

    // then
    assertTrue(dmRoom.getOutUsers().contains(senderId)); // 자동 추가됨
    assertTrue(dmRoom.getOutUsers().contains(receiverId)); // receiver도 나감
    verify(dmRoomRepository).save(dmRoom);
  }

  @Test
  @DisplayName("나간 유저가 findOrCreateRoom 호출 시 reenterRoom 통해 재진입된다")
  void reenterAfterOut() {
    // given
    DmRoom room = new DmRoom(senderId, receiverId);
    room.addOutUser(senderId); // A가 나간 상태라고 가정

    when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
    when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
    when(dmRoomRepository.findByRoomBetweenUsers(senderId, receiverId)).thenReturn(Optional.of(room));
    when(dmRoomRepository.findById(room.getId())).thenReturn(Optional.of(room));

    // when
    DmRoomDto dto = dmRoomService.findOrCreateRoom(senderId, receiverId);

    // then
    verify(dmRoomService).reenterRoom(eq(senderId), eq(room.getId()));
    assertFalse(room.isOut(senderId));                          // outUsers 에서 제거됐는지
    Assertions.assertEquals(room.getId(), dto.getId());        // 기존 방 재사용 확인
  }

  @Test
  @DisplayName("getAllRoomsForUser - updatedAt 순으로 내림차순 정렬되는지 테스트")
  void getAllRoomsForUser_sortedByUpdatedAt() {
    // 준비: userId
    UUID userId = UUID.randomUUID();

    // baseTime 기준으로 10개 방 생성
    LocalDateTime baseTime = LocalDateTime.of(2025, 7, 25, 12, 0);
    List<DmRoom> rooms = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      // id, sender, receiver 아무 UUID라도 상관없음
      DmRoom room = new DmRoom(UUID.randomUUID(), userId, UUID.randomUUID());

      // createdAt, updatedAt 세팅 (i가 클수록 최신)
      ReflectionTestUtils.setField(room, "createdAt", baseTime.plusMinutes(i));
      ReflectionTestUtils.setField(room, "lastMessageAt", baseTime.plusMinutes(i));

      // 메시지가 없어도 되지만, lastMessage 로직을 위해 하나라도 넣어둘 수 있습니다
      Dm dummy = new Dm(userId, "msg" + i);
      dummy.setDmRoom(room);
      if(i==10){
        room.getMessages().add(new Dm(userId, "hello world"));
      }else{
        room.getMessages().add(dummy);
      }

      rooms.add(room);
    }

    // mock: repository, userRepository, unreadCount
    given(dmRoomRepository.findBySenderIdOrReceiverId(any(), any())).willReturn(rooms);

    // userRepository는 senderId, receiverId 둘 다 찾도록
    for (DmRoom room : rooms) {
      User sender = User.builder().id(room.getSenderId()).email("sender@test.com").name("sender").password("pw").build();
      User receiver = User.builder().id(room.getReceiverId()).email("receiver@test.com").name("receiver").password("pw").build();

      when(userRepository.findById(room.getSenderId()))
          .thenReturn(Optional.of(sender));
      when(userRepository.findById(room.getReceiverId()))
          .thenReturn(Optional.of(receiver));
    }
    // unreadCount는 0으로 고정
    
  (0).when(dmRoomService).getUnreadCount(any(), eq(userId));

    // 실행
    List<DmRoomDto> result = dmRoomService.getAllRoomsForUser(userId);

    // 검증: 가장 첫 번째 방의 updatedAt 이 baseTime + 10분 인지 확인
    DmRoomDto first = result.get(0);
    assertThat(first.getNewMessageCount()).isEqualTo(0);
    assertThat(first.getLastMessage()).isEqualTo("hello world");
    assertThat(first.getLastMessageAt()).isEqualTo(baseTime.plusMinutes(10));
  }

}
