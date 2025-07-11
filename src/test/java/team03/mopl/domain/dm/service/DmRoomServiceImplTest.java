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
import org.springframework.security.crypto.password.PasswordEncoder;
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
    DmRoom room2 = new DmRoom(roomId2, senderId, receiverId);
    room2.getMessages().add(message2);

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
  @DisplayName("deleteRoom - sender가 나가고, 이후 receiver도 나가서 방이 삭제된다")
  void deleteRoom() {
    // given
    UUID roomId = UUID.randomUUID();

    // 메시지
    UUID messageId = UUID.randomUUID();
    Dm message = new Dm(messageId, senderId, "테스트 메시지");

    // 방
    DmRoom dmRoom = new DmRoom(roomId, senderId, receiverId);
    dmRoom.getMessages().add(message);

    // 방 조회
    given(dmRoomRepository.findById(roomId)).willReturn(Optional.of(dmRoom));

    // 첫번째: sender가 나감
    dmRoomService.deleteRoom(senderId, roomId);

    // senderId가 null이 됐는지 확인
    assertThat(dmRoom.getSenderId()).isNull();
    assertThat(dmRoom.getReceiverId()).isEqualTo(receiverId);

    // 두번째: receiver도 나감
    dmRoomService.deleteRoom(receiverId, roomId);

    // 이제 둘 다 null → nobodyInRoom → 삭제
    then(dmService).should().deleteDm(messageId);
    then(dmRoomRepository).should().delete(dmRoom);
  }

}