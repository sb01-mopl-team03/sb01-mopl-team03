package team03.mopl.domain.chat.service;

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
import team03.mopl.common.exception.MoplException;
import team03.mopl.domain.chat.dto.ChatMessageCreateRequest;
import team03.mopl.domain.chat.dto.ChatMessageDto;
import team03.mopl.domain.chat.entity.ChatMessage;
import team03.mopl.domain.chat.entity.ChatRoom;
import team03.mopl.domain.chat.exception.ChatException;
import team03.mopl.domain.chat.repository.ChatMessageRepository;
import team03.mopl.domain.chat.repository.ChatRoomParticipantRepository;
import team03.mopl.domain.chat.repository.ChatRoomRepository;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
//import team03.mopl.domain.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅 메세지 서비스 테스트")
class ChatMessageServiceImplTest {

  //@Mock
  //private UserRepository userRepository;

  @Mock
  private ChatMessageRepository chatMessageRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRoomParticipantRepository chatRoomParticipantRepository;

  @InjectMocks
  private ChatMessageServiceImpl chatMessageService;

  // 테스트용 유저
  private UUID senderId;
  private User sender;

  // 테스트용 콘텐츠
  private UUID contentId;
  private Content content;

  // 테스트용 채팅방
  private UUID chatRoomId;
  private ChatRoom chatRoom;


  @BeforeEach
  void setUp() {
    senderId = UUID.randomUUID();
    sender = new User(
        "test@test.com",
        "테스트유저",
        "testpassword",
        Role.USER
    );

    contentId = UUID.randomUUID();
    content = new Content(
        "테스트콘텐츠",
        "테스트용 콘텐츠 입니다.",
        ContentType.DRAMA,
        LocalDateTime.now()
    );

    chatRoomId = UUID.randomUUID();
    chatRoom = new ChatRoom(content);
  }

  @Nested
  @DisplayName("채팅 메세지 생성")
  class CreateChatMessage {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      LocalDateTime now = LocalDateTime.now();

      ChatMessageCreateRequest request = new ChatMessageCreateRequest(
          chatRoomId,
          senderId,
          "테스트용 채팅 메세지입니다.",
          now
      );

      UUID chatMessageId = UUID.randomUUID();
      ChatMessageDto expected = new ChatMessageDto(chatMessageId, senderId, chatRoomId,
          "테스트용 채팅 메세지입니다.", now);

      //when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
      when(chatRoomParticipantRepository
          .existsChatRoomParticipantByChatRoomAndUser(chatRoom, sender)).thenReturn(true);

      //when
      ChatMessageDto result = chatMessageService.create(request);

      //then
      assertNotNull(result);
      assertEquals(expected.senderId(), result.senderId());
      assertEquals(expected.chatRoomId(), result.chatRoomId());
      assertEquals(expected.content(), result.content());

      verify(chatMessageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("존재하지 않는 작성자")
    void failsWhenUserNotFound() {
      //given
      LocalDateTime now = LocalDateTime.now();

      UUID randomUserId = UUID.randomUUID();

      ChatMessageCreateRequest request = new ChatMessageCreateRequest(
          chatRoomId,
          randomUserId,
          "테스트용 채팅 메세지입니다.",
          now
      );

      //when(userRepository.findById(senderId)).thenReturn(Optional.empty());

      //when & then
      //todo - UserNotFound 로 수정
      assertThrows(MoplException.class, () -> chatMessageService.create(request));

      verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방")
    void failsWhenChatRoomNotFound() {
      //given
      LocalDateTime now = LocalDateTime.now();

      UUID randomChatRoomId = UUID.randomUUID();

      ChatMessageCreateRequest request = new ChatMessageCreateRequest(
          randomChatRoomId,
          senderId,
          "테스트용 채팅 메세지입니다.",
          now
      );

      //when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

      //when & then
      //todo - 예외수정
      assertThrows(ChatException.class, () -> chatMessageService.create(request));

      verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    @DisplayName("채팅방에 존재하지 않는 유저")
    void failsWhenUserNotParticipant() {
      //given
      LocalDateTime now = LocalDateTime.now();

      ChatMessageCreateRequest request = new ChatMessageCreateRequest(
          chatRoomId,
          senderId,
          "테스트용 채팅 메세지입니다.",
          now
      );

      //when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
      when(chatRoomParticipantRepository
          .existsChatRoomParticipantByChatRoomAndUser(chatRoom, sender)).thenReturn(false);

      //when & then
      //todo - 예외수정
      assertThrows(ChatException.class, () -> chatMessageService.create(request));

      verify(chatMessageRepository, never()).save(any(ChatMessage.class));

    }
  }

  @Nested
  @DisplayName("채팅 메세지 조회 테스트")
  class getChatMessage {

    //todo - 페이지네이션 조회

    @Test
    @DisplayName("성공")
    void success() {

      //given
      LocalDateTime now = LocalDateTime.now();

      ChatMessage mockChatMessage1 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 첫번째 채팅 메세지입니다."
      );
      ChatMessage mockChatMessage2 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 두번째 채팅 메세지입니다."
      );
      List<ChatMessage> searchResult = new ArrayList<>();
      searchResult.add(mockChatMessage1);
      searchResult.add(mockChatMessage2);

      UUID mockChatMessage1Id = UUID.randomUUID();
      UUID mockChatMessage2Id = UUID.randomUUID();
      ChatMessageDto expected1 = new ChatMessageDto(mockChatMessage1Id, senderId, chatRoomId,
          "테스트용 첫번째 채팅 메세지입니다.", now);
      ChatMessageDto expected2 = new ChatMessageDto(mockChatMessage2Id, senderId, chatRoomId,
          "테스트용 두번째 채팅 메세지입니다.", now);

      List<ChatMessageDto> expected = new ArrayList<>();
      expected.add(expected1);
      expected.add(expected2);

      //when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
      when(chatRoomParticipantRepository
          .existsChatRoomParticipantByChatRoomAndUser(chatRoom, sender))
          .thenReturn(true);

      //when
      List<ChatMessageDto> result = chatMessageService.getAllByRoomId(chatRoomId);

      //then
      assertNotNull(result);
      assertEquals(expected.size(), result.size());
      assertEquals(expected.get(0), result.get(0));
    }


    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      //given
      LocalDateTime now = LocalDateTime.now();

      ChatMessage mockChatMessage1 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 첫번째 채팅 메세지입니다."
      );
      ChatMessage mockChatMessage2 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 두번째 채팅 메세지입니다."
      );
      List<ChatMessage> searchResult = new ArrayList<>();
      searchResult.add(mockChatMessage1);
      searchResult.add(mockChatMessage2);

      UUID mockChatMessage1Id = UUID.randomUUID();
      UUID mockChatMessage2Id = UUID.randomUUID();
      ChatMessageDto expected1 = new ChatMessageDto(mockChatMessage1Id, senderId, chatRoomId,
          "테스트용 첫번째 채팅 메세지입니다.", now);
      ChatMessageDto expected2 = new ChatMessageDto(mockChatMessage2Id, senderId, chatRoomId,
          "테스트용 두번째 채팅 메세지입니다.", now);

      List<ChatMessageDto> expected = new ArrayList<>();
      expected.add(expected1);
      expected.add(expected2);

      //when(userRepository.findById(senderId)).thenReturn(Optional.empty());

      //when & then
      //todo - UserNotFound 로 수정
      assertThrows(MoplException.class, () -> chatMessageService.getAllByRoomId(chatRoomId));

    }

    @Test
    @DisplayName("존재하지 않는 채팅방")
    void failsWhenChatRoomNotFound() {
      //given
      LocalDateTime now = LocalDateTime.now();

      ChatMessage mockChatMessage1 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 첫번째 채팅 메세지입니다."
      );
      ChatMessage mockChatMessage2 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 두번째 채팅 메세지입니다."
      );
      List<ChatMessage> searchResult = new ArrayList<>();
      searchResult.add(mockChatMessage1);
      searchResult.add(mockChatMessage2);

      UUID mockChatMessage1Id = UUID.randomUUID();
      UUID mockChatMessage2Id = UUID.randomUUID();
      ChatMessageDto expected1 = new ChatMessageDto(mockChatMessage1Id, senderId, chatRoomId,
          "테스트용 첫번째 채팅 메세지입니다.", now);
      ChatMessageDto expected2 = new ChatMessageDto(mockChatMessage2Id, senderId, chatRoomId,
          "테스트용 두번째 채팅 메세지입니다.", now);

      List<ChatMessageDto> expected = new ArrayList<>();
      expected.add(expected1);
      expected.add(expected2);

      //when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.empty());

      //when & then
      //todo - 예외수정
      assertThrows(ChatException.class,
          () -> chatMessageService.getAllByRoomId(chatRoomId));
    }

    @Test
    @DisplayName("채팅방에 참여하지 않는 유저")
    void failsWhenUserNotParticipant() {
      //given
      LocalDateTime now = LocalDateTime.now();

      ChatMessage mockChatMessage1 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 첫번째 채팅 메세지입니다."
      );
      ChatMessage mockChatMessage2 = new ChatMessage(
          sender,
          chatRoom,
          "테스트용 두번째 채팅 메세지입니다."
      );
      List<ChatMessage> searchResult = new ArrayList<>();
      searchResult.add(mockChatMessage1);
      searchResult.add(mockChatMessage2);

      UUID mockChatMessage1Id = UUID.randomUUID();
      UUID mockChatMessage2Id = UUID.randomUUID();
      ChatMessageDto expected1 = new ChatMessageDto(mockChatMessage1Id, senderId, chatRoomId,
          "테스트용 첫번째 채팅 메세지입니다.", now);
      ChatMessageDto expected2 = new ChatMessageDto(mockChatMessage2Id, senderId, chatRoomId,
          "테스트용 두번째 채팅 메세지입니다.", now);

      List<ChatMessageDto> expected = new ArrayList<>();
      expected.add(expected1);
      expected.add(expected2);

      //when(userRepository.findById(senderId)).thenReturn(Optional.of(sender));
      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
      when(chatRoomParticipantRepository
          .existsChatRoomParticipantByChatRoomAndUser(chatRoom, sender))
          .thenReturn(false);

      //when & then
      //todo - 예외수정
      assertThrows(ChatException.class, () -> chatMessageService.getAllByRoomId(chatRoomId));
    }
  }
}