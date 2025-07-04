package team03.mopl.domain.watchroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomMessage;
import team03.mopl.domain.watchroom.exception.WatchRoomRoomNotFoundException;
import team03.mopl.domain.watchroom.repository.WatchRoomMessageRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomParticipantRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomRepository;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅 메세지 서비스 테스트")
class WatchRoomMessageServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private WatchRoomMessageRepository watchRoomMessageRepository;

  @Mock
  private WatchRoomRepository watchRoomRepository;

  @Mock
  private WatchRoomParticipantRepository watchRoomParticipantRepository;

  @InjectMocks
  private WatchRoomMessageServiceImpl chatMessageService;

  // 테스트용 유저
  private UUID senderId;
  private User sender;

  // 테스트용 콘텐츠
  private UUID contentId;
  private Content content;

  // 테스트용 채팅방
  private UUID chatRoomId;
  private WatchRoom watchRoom;


  @BeforeEach
  void setUp() {
    senderId = UUID.randomUUID();
    sender = User.builder()
        .id(senderId)
        .name("테스트유저")
        .email("test@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    contentId = UUID.randomUUID();
    content = Content.builder()
        .title("테스트콘텐츠")
        .description("테스트용 콘텐츠 입니다.")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .build();

    chatRoomId = UUID.randomUUID();
    watchRoom = WatchRoom.builder()
        .id(chatRoomId)
        .content(content)
        .build();
  }

  @Nested
  @DisplayName("채팅 메세지 생성")
  class CreateWatchRoomMessage {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      LocalDateTime now = LocalDateTime.now();

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          chatRoomId,
          senderId,
          "테스트용 채팅 메세지입니다.",
          now
      );

      UUID chatMessageId = UUID.randomUUID();
      WatchRoomMessageDto expected = new WatchRoomMessageDto(chatMessageId, senderId, chatRoomId,
          "테스트용 채팅 메세지입니다.", now);

      WatchRoomMessage watchRoomMessage = WatchRoomMessage.builder()
          .id(chatMessageId)
          .watchRoom(watchRoom)
          .content(request.content())
          .sender(sender)
          .build();


      when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository
          .existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, sender)).thenReturn(true);
      when(watchRoomMessageRepository.save(any(WatchRoomMessage.class))).thenReturn(watchRoomMessage);

      //when
      WatchRoomMessageDto result = chatMessageService.create(request);

      //then
      assertNotNull(result);
      assertEquals(expected.senderId(), result.senderId());
      assertEquals(expected.chatRoomId(), result.chatRoomId());
      assertEquals(expected.content(), result.content());

      verify(watchRoomMessageRepository, times(1)).save(any(WatchRoomMessage.class));
    }

    @Test
    @DisplayName("존재하지 않는 작성자")
    void failsWhenUserNotFound() {
      //given
      UUID randomUserId = UUID.randomUUID();

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          chatRoomId,
          randomUserId,
          "테스트용 채팅 메세지입니다.",
          LocalDateTime.now()
      );

      when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class, () -> chatMessageService.create(request));

      verify(watchRoomMessageRepository, never()).save(any(WatchRoomMessage.class));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방")
    void failsWhenChatRoomNotFound() {
      //given
      LocalDateTime now = LocalDateTime.now();

      UUID randomChatRoomId = UUID.randomUUID();

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          randomChatRoomId,
          senderId,
          "테스트용 채팅 메세지입니다.",
          now
      );

      when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(watchRoomRepository.findById(randomChatRoomId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class, () -> chatMessageService.create(request));

      verify(watchRoomMessageRepository, never()).save(any(WatchRoomMessage.class));
    }

    @Test
    @DisplayName("채팅방에 존재하지 않는 유저")
    void failsWhenUserNotParticipant() {
      //given
      LocalDateTime now = LocalDateTime.now();

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          chatRoomId,
          senderId,
          "테스트용 채팅 메세지입니다.",
          now
      );

      when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository
          .existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, sender)).thenReturn(false);

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class, () -> chatMessageService.create(request));

      verify(watchRoomMessageRepository, never()).save(any(WatchRoomMessage.class));
    }
  }

  @Nested
  @DisplayName("채팅 메세지 조회 테스트")
  class getWatchRoomMessage {

    //todo - 페이지네이션 조회

    @Test
    @DisplayName("성공")
    void success() {

      //given
      LocalDateTime now = LocalDateTime.now();

      WatchRoomMessage mockWatchRoomMessage1 = WatchRoomMessage.builder()
          .sender(sender)
          .watchRoom(watchRoom)
          .content("테스트용 첫번째 채팅 메세지입니다.")
          .build();
      WatchRoomMessage mockWatchRoomMessage2 = WatchRoomMessage.builder()
          .sender(sender)
          .watchRoom(watchRoom)
          .content("테스트용 두번째 채팅 메세지입니다.")
          .build();

      List<WatchRoomMessage> searchResult = new ArrayList<>();
      searchResult.add(mockWatchRoomMessage1);
      searchResult.add(mockWatchRoomMessage2);

      UUID mockChatMessage1Id = UUID.randomUUID();
      UUID mockChatMessage2Id = UUID.randomUUID();
      WatchRoomMessageDto expected1 = new WatchRoomMessageDto(mockChatMessage1Id, senderId, chatRoomId,
          "테스트용 첫번째 채팅 메세지입니다.", now);
      WatchRoomMessageDto expected2 = new WatchRoomMessageDto(mockChatMessage2Id, senderId, chatRoomId,
          "테스트용 두번째 채팅 메세지입니다.", now);

      List<WatchRoomMessageDto> expected = new ArrayList<>();
      expected.add(expected1);
      expected.add(expected2);

      //todo - 고민
      //네번이나 조회할 필요가 있을까?
      // 이것도 chatRoomParticipantRepository에서 chatRoomId로 찾지못하면 없는 채널로 판단하기?
      when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository
          .existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, sender))
          .thenReturn(true);
      when(watchRoomMessageRepository.findAllByWatchRoom(watchRoom)).thenReturn(searchResult);

      //when
      List<WatchRoomMessageDto> result = chatMessageService.getAllByRoomId(chatRoomId,senderId);

      //then
      assertNotNull(result);
      assertEquals(expected.size(), result.size());
      assertEquals(expected.get(0).content(), result.get(0).content());
    }


    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      //given
      UUID randomId = UUID.randomUUID();

      when(userRepository.findById(randomId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> chatMessageService.getAllByRoomId(chatRoomId, randomId));

    }

    @Test
    @DisplayName("존재하지 않는 채팅방")
    void failsWhenChatRoomNotFound() {
      //given
      UUID randomId = UUID.randomUUID();

      when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(watchRoomRepository.findById(randomId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class,
          () -> chatMessageService.getAllByRoomId(randomId, senderId));
    }

    @Test
    @DisplayName("채팅방에 참여하지 않는 유저")
    void failsWhenUserNotParticipant() {
      //given
      UUID randomId = UUID.randomUUID();
      User mockUser = User.builder().build();

      when(userRepository.findById(randomId)).thenReturn(Optional.of(mockUser));
      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository
          .existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, mockUser))
          .thenReturn(false);

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class,
          () -> chatMessageService.getAllByRoomId(chatRoomId, randomId));
    }
  }
}