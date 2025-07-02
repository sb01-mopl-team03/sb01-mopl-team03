package team03.mopl.domain.chat.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.chat.dto.ChatRoomContentWithHeadcountDto;
import team03.mopl.domain.chat.dto.ChatRoomCreateRequest;
import team03.mopl.domain.chat.dto.ChatRoomDto;
import team03.mopl.domain.chat.entity.ChatRoom;
import team03.mopl.domain.chat.entity.ChatRoomParticipant;
import team03.mopl.domain.chat.exception.ChatRoomNotFoundException;
import team03.mopl.domain.chat.repository.ChatRoomParticipantRepository;
import team03.mopl.domain.chat.repository.ChatRoomRepository;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅방 서비스 테스트")
class ChatRoomServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ChatRoomRepository chatRoomRepository;

  @Mock
  private ChatRoomParticipantRepository chatRoomParticipantRepository;

  @InjectMocks
  private ChatRoomServiceImpl chatRoomService;

  private UUID contentId;
  private Content content;
  private UUID userId;
  private User user;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .name("테스트유저")
        .email("test@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    //todo - 빌더 또는 생성자 구현해주시면 수정하기
    contentId = UUID.randomUUID();
    content = Content.builder()
        .title("테스트콘텐츠")
        .description("테스트용 콘텐츠 입니다.")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .build();
  }

  @Nested
  @DisplayName("채팅방 생성 테스트")
  class createChatRoom {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      ChatRoomCreateRequest request = new ChatRoomCreateRequest(
          contentId,
          userId
      );

      UUID chatRoomId = UUID.randomUUID();
      ChatRoom chatRoom = ChatRoom.builder()
          .id(chatRoomId)
          .content(content)
          .ownerId(userId)
          .build();

      ChatRoomDto expected = ChatRoomDto.fromChatRoomWithHeadcount(chatRoom,1L);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);

      //when
      ChatRoomDto chatRoomDto = chatRoomService.create(request);

      //then
      assertEquals(expected.contentId(), chatRoomDto.contentId());
      assertEquals(expected.ownerId(), chatRoomDto.ownerId());
      assertEquals(expected.headCount(), chatRoomDto.headCount());
    }

    @Test
    @DisplayName("실패")
    void failsWhenContentNotFound() {
      //given
      UUID randomId = UUID.randomUUID();

      ChatRoomCreateRequest request = new ChatRoomCreateRequest(
          randomId,
          userId
      );

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when & then
      assertThrows(ContentNotFoundException.class, () -> chatRoomService.create(request));

      verify(chatRoomRepository, never()).save(any(ChatRoom.class));
    }

  }

  @Nested
  @DisplayName("채팅방 전체 조회 테스트")
  class getAllChatRoom {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID chatRoom1Id = UUID.randomUUID();
      ChatRoom chatRoom1 = ChatRoom.builder()
          .id(chatRoom1Id)
          .ownerId(userId)
          .content(content)
          .build();

      UUID user2Id = UUID.randomUUID();
      UUID chatRoom2Id = UUID.randomUUID();
      ChatRoom chatRoom2 = ChatRoom.builder()
          .id(chatRoom2Id)
          .ownerId(user2Id)
          .content(content)
          .build();

      List<ChatRoom> chatRooms = List.of(chatRoom1, chatRoom2);

      List<ChatRoomContentWithHeadcountDto> queryResult = chatRooms.stream()
          .map(c-> new ChatRoomContentWithHeadcountDto(c, content, 1L ))
          .toList();

      List<ChatRoomDto> expected = chatRooms.stream()
          .map(chatRoom -> ChatRoomDto.fromChatRoomWithHeadcount(chatRoom, 1L))
          .toList();

      when(chatRoomParticipantRepository.getAllChatRoomContentWithHeadcountDto()).thenReturn(queryResult);
      //when
      List<ChatRoomDto> chatRoomDtos = chatRoomService.getAll();

      //then
      assertEquals(expected.size(), chatRoomDtos.size());
    }

    @Test
    @DisplayName("실패")
    void fails() {
      //조회 실패 케이스가 있을까? (페이지네이션 제외)
    }
  }

  @Nested
  @DisplayName("채팅방 개별 조회 테스트")
  class getChatRoomById {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID chatRoomId = UUID.randomUUID();
      ChatRoom chatRoom = ChatRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .build();

      ChatRoomDto expected = ChatRoomDto.fromChatRoomWithHeadcount(chatRoom, 1L);

      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
      when(chatRoomParticipantRepository.countByChatRoomId(chatRoomId)).thenReturn(1L);

      //when
      ChatRoomDto chatRoomDto = chatRoomService.getById(chatRoomId);

      //then
      assertEquals(expected.id(), chatRoomDto.id());
      assertEquals(expected.ownerId(), chatRoomDto.ownerId());
      assertEquals(expected.contentId(), chatRoomDto.contentId());
      assertEquals(expected.headCount(), chatRoomDto.headCount());

    }

    @Test
    @DisplayName("실패")
    void failsWhenChatRoomNotFound() {
      UUID randomId = UUID.randomUUID();

      when(chatRoomRepository.findById(randomId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(ChatRoomNotFoundException.class, () -> chatRoomService.getById(randomId));

      verify(chatRoomParticipantRepository, never()).countByChatRoomId(randomId);
    }
  }

  @Nested
  @DisplayName("채탕방 참여")
  class participateChatRoom {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID participantId = UUID.randomUUID();
      User participant = User.builder()
          .id(participantId)
          .name("채팅방참여자")
          .email("participant@test.com")
          .password("participant_password")
          .role(Role.USER)
          .build();

      UUID chatRoomId = UUID.randomUUID();
      ChatRoom chatRoom = ChatRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .build();

      ChatRoomDto expected = ChatRoomDto.fromChatRoomWithHeadcount(chatRoom, 2L);
      //todo - 논의
      // 참여자의 정보들(참여자목록)도 같이 넘겨주어야할까?

      when(userRepository.findById(participantId)).thenReturn(Optional.of(participant));
      when(chatRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(chatRoom));
      when(chatRoomParticipantRepository.existsChatRoomParticipantByChatRoomAndUser(chatRoom, participant))
          .thenReturn(false);
      when(chatRoomParticipantRepository.countByChatRoomId(chatRoomId)).thenReturn(2L);

      //when
      ChatRoomDto chatRoomDto = chatRoomService.join(chatRoomId ,participantId);

      //then
      assertEquals(expected.id(), chatRoomDto.id());
      assertEquals(expected.ownerId(), chatRoomDto.ownerId());
      assertEquals(expected.contentId(), chatRoomDto.contentId());
      assertEquals(expected.headCount(), chatRoomDto.headCount());
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      //given
      UUID randomId = UUID.randomUUID();

      UUID chatRoomId = UUID.randomUUID();
      ChatRoom chatRoom = ChatRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .build();

      //when & then
      assertThrows(UserNotFoundException.class, ()->  chatRoomService.join(chatRoomId, randomId));

      verify(chatRoomParticipantRepository, never()).save(any(ChatRoomParticipant.class));
      verify(chatRoomParticipantRepository, never()).countByChatRoomId(chatRoomId);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방")
    void failsWhenChatRoomNotFound() {
      //given
      UUID participantId = UUID.randomUUID();
      User participant = User.builder()
          .id(participantId)
          .name("채팅방참여자")
          .email("participant@test.com")
          .password("participant_password")
          .role(Role.USER)
          .build();

      UUID randomId = UUID.randomUUID();

      when(userRepository.findById(participantId)).thenReturn(Optional.of(user));
      //todo - 로직 고민
      //chatRoomRepository 에서 채널 있는지 검사 -> chatRoomParticipantRepository 로 이미 참여한 유저인지 검사
      // vs chatRoomParticipantRepository 로 채널 있는지 검사 -> 같은 걸로 이미 참여한 유저인지 검사

      //when & then
      assertThrows(ChatRoomNotFoundException.class , () -> chatRoomService.join(randomId, participantId));

      verify(chatRoomParticipantRepository, never()).save(any(ChatRoomParticipant.class));
      verify(chatRoomParticipantRepository, never()).countByChatRoomId(randomId);
    }
  }
}
