package team03.mopl.domain.watchroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithHeadcountDto;
import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;
import team03.mopl.domain.watchroom.dto.ParticipantDto;
import team03.mopl.domain.watchroom.dto.ParticipantsInfoDto;
import team03.mopl.domain.watchroom.dto.VideoControlRequest;
import team03.mopl.domain.watchroom.dto.VideoSyncDto;
import team03.mopl.domain.watchroom.dto.WatchRoomInfoDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomParticipant;
import team03.mopl.domain.watchroom.entity.VideoControlAction;
import team03.mopl.domain.watchroom.exception.WatchRoomRoomNotFoundException;
import team03.mopl.domain.watchroom.repository.WatchRoomParticipantRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomRepository;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("채팅방 서비스 테스트")
class WatchRoomServiceImplTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private WatchRoomRepository watchRoomRepository;

  @Mock
  private WatchRoomParticipantRepository watchRoomParticipantRepository;

  @InjectMocks
  private WatchRoomServiceImpl chatRoomService;

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
  class createWatchRoom {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      WatchRoomCreateRequest request = new WatchRoomCreateRequest(
          contentId,
          userId
      );

      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .content(content)
          .ownerId(userId)
          .build();

      WatchRoomDto expected = WatchRoomDto.fromChatRoomWithHeadcount(watchRoom, 1L);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      WatchRoomDto watchRoomDto = chatRoomService.create(request);

      //then
      assertEquals(expected.contentTitle(), watchRoomDto.contentTitle());
      assertEquals(expected.ownerId(), watchRoomDto.ownerId());
      assertEquals(expected.headCount(), watchRoomDto.headCount());
    }

    @Test
    @DisplayName("실패")
    void failsWhenContentNotFound() {
      //given
      UUID randomId = UUID.randomUUID();

      WatchRoomCreateRequest request = new WatchRoomCreateRequest(
          randomId,
          userId
      );

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      //when & then
      assertThrows(ContentNotFoundException.class, () -> chatRoomService.create(request));

      verify(watchRoomRepository, never()).save(any(WatchRoom.class));
    }

  }

  @Nested
  @DisplayName("채팅방 전체 조회 테스트")
  class getAllWatchRoom {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID chatRoom1Id = UUID.randomUUID();
      WatchRoom watchRoom1 = WatchRoom.builder()
          .id(chatRoom1Id)
          .ownerId(userId)
          .content(content)
          .build();

      UUID user2Id = UUID.randomUUID();
      UUID chatRoom2Id = UUID.randomUUID();
      WatchRoom watchRoom2 = WatchRoom.builder()
          .id(chatRoom2Id)
          .ownerId(user2Id)
          .content(content)
          .build();

      List<WatchRoom> watchRooms = List.of(watchRoom1, watchRoom2);

      List<WatchRoomContentWithHeadcountDto> queryResult = watchRooms.stream()
          .map(c -> new WatchRoomContentWithHeadcountDto(c, content, 1L))
          .toList();

      List<WatchRoomDto> expected = watchRooms.stream()
          .map(chatRoom -> WatchRoomDto.fromChatRoomWithHeadcount(chatRoom, 1L))
          .toList();

      when(watchRoomParticipantRepository.getAllChatRoomContentWithHeadcountDto()).thenReturn(
          queryResult);
      //when
      List<WatchRoomDto> watchRoomDtos = chatRoomService.getAll();

      //then
      assertEquals(expected.size(), watchRoomDtos.size());
    }

    @Test
    @DisplayName("실패")
    void fails() {
      //조회 실패 케이스가 있을까? (페이지네이션 제외)
    }
  }

  @Nested
  @DisplayName("채팅방 개별 조회 테스트")
  class getWatchRoomById {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .build();

      WatchRoomDto expected = WatchRoomDto.fromChatRoomWithHeadcount(watchRoom, 1L);

      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository.countByChatRoomId(chatRoomId)).thenReturn(1L);

      //when
      WatchRoomDto watchRoomDto = chatRoomService.getById(chatRoomId);

      //then
      assertEquals(expected.id(), watchRoomDto.id());
      assertEquals(expected.ownerId(), watchRoomDto.ownerId());
      assertEquals(expected.contentTitle(), watchRoomDto.contentTitle());
      assertEquals(expected.headCount(), watchRoomDto.headCount());

    }

    @Test
    @DisplayName("실패")
    void failsWhenChatRoomNotFound() {
      UUID randomId = UUID.randomUUID();

      when(watchRoomRepository.findById(randomId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class, () -> chatRoomService.getById(randomId));

      verify(watchRoomParticipantRepository, never()).countByChatRoomId(randomId);
    }
  }

  @Nested
  @DisplayName("채탕방 참여 후 정보 반환")
  class joinWatchRoomAndGetInfo {

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

      List<User> users = List.of(participant, user);

      List<ParticipantDto> participantDtos = users.stream().map(user -> {
            return ParticipantDto.builder()
                .username(user.getName())
                .profile(null)
                .isOwner(user.getId() == userId)
                .build();
          }
      ).toList();

      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .build();

      List<WatchRoomParticipant> watchRoomParticipant = users.stream().map(
          user -> {
            return WatchRoomParticipant.builder().user(user).watchRoom(watchRoom).build();
          }
      ).toList();

      ParticipantsInfoDto participantsInfoDto = new ParticipantsInfoDto(participantDtos,
          participantDtos.size());

      WatchRoomInfoDto expected = WatchRoomInfoDto.builder()
          .id(chatRoomId)
          .contentTitle(watchRoom.getContent().getTitle())
          .participantsInfoDto(participantsInfoDto)
          .build();

      when(userRepository.findById(participantId)).thenReturn(Optional.of(participant));
      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository.existsChatRoomParticipantByChatRoomAndUser(watchRoom,
          participant))
          .thenReturn(false);
      when(watchRoomParticipantRepository.findByChatRoom(watchRoom)).thenReturn(watchRoomParticipant);

      //when
      WatchRoomInfoDto watchRoomInfoDto = chatRoomService.joinChatRoomAndGetInfo(chatRoomId,
          participantId);

      //todo - verify로 확인하기
      assertEquals(expected.id(), watchRoomInfoDto.id());
      assertEquals(expected.contentTitle(), watchRoomInfoDto.contentTitle());
      assertEquals(expected.participantsInfoDto().participantsCount(),
          watchRoomInfoDto.participantsInfoDto().participantsCount());

      verify(watchRoomParticipantRepository, times(1)).save(any(WatchRoomParticipant.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      //given
      UUID randomId = UUID.randomUUID();

      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .build();

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> chatRoomService.joinChatRoomAndGetInfo(chatRoomId, randomId));

      verify(watchRoomParticipantRepository, never()).save(any(WatchRoomParticipant.class));
      verify(watchRoomParticipantRepository, never()).countByChatRoomId(chatRoomId);
    }

    @Test
    @DisplayName("존재하지 않는 채팅방")
    void failsWhenChatRoomNotFound() {
      //given
      UUID participantId = UUID.randomUUID();

      UUID randomId = UUID.randomUUID();

      when(userRepository.findById(participantId)).thenReturn(Optional.of(user));
      //todo - 로직 고민
      //chatRoomRepository 에서 채널 있는지 검사 -> chatRoomParticipantRepository 로 이미 참여한 유저인지 검사
      // vs chatRoomParticipantRepository 로 채널 있는지 검사 -> 같은 걸로 이미 참여한 유저인지 검사

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class,
          () -> chatRoomService.joinChatRoomAndGetInfo(randomId, participantId));

      verify(watchRoomParticipantRepository, never()).save(any(WatchRoomParticipant.class));
      verify(watchRoomParticipantRepository, never()).countByChatRoomId(randomId);
    }
  }

  @Nested
  @DisplayName("시청방 유저 목록 조회")
  class getParticipants{

    @Test
    @DisplayName("성공")
    void success() {

    }
  }

  @Nested
  @DisplayName("시청방 상세 정보 조회")
  class getWatchRoomInfo {
    @Test
    @DisplayName("성공")
    void success() {

    }
  }

  @Nested
  @DisplayName("시청방 비디오 제어")
  class updateVideoStatus{
    @Test
    @DisplayName("일시 정지 성공")
    void successPause() {
      //given
      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .videoStateUpdatedAt(LocalDateTime.now())
          .isPlaying(true)
          .currentTime(10.0)
          .build();

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.PAUSE, 10.0);

      VideoSyncDto expected = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.PAUSE)
          .isPlaying(false)
          .timestamp(System.currentTimeMillis())
          .build();

      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      VideoSyncDto videoSyncDto = chatRoomService.updateVideoStatus(chatRoomId, request, userId);

      //then
      assertEquals(expected.videoControlAction(), videoSyncDto.videoControlAction());
      assertEquals(expected.isPlaying(), videoSyncDto.isPlaying());

      verify(watchRoomRepository, times(1)).save(watchRoom);

    }

    @Test
    @DisplayName("재생 성공")
    void successPlay() {
      //given
      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .videoStateUpdatedAt(LocalDateTime.now())
          .isPlaying(false)
          .currentTime(10.0)
          .build();

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.PLAY, 10.0);

      VideoSyncDto expected = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.PLAY)
          .isPlaying(true)
          .timestamp(System.currentTimeMillis())
          .build();

      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      VideoSyncDto videoSyncDto = chatRoomService.updateVideoStatus(chatRoomId, request, userId);

      //then
      assertEquals(expected.videoControlAction(), videoSyncDto.videoControlAction());
      assertEquals(expected.isPlaying(), videoSyncDto.isPlaying());

      verify(watchRoomRepository, times(1)).save(watchRoom);

    }

    @Test
    @DisplayName("특정 시간으로 이동 성공")
    void successSeek() {
      //given
      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .ownerId(userId)
          .content(content)
          .videoStateUpdatedAt(LocalDateTime.now())
          .isPlaying(true)
          .currentTime(10.0)
          .build();

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.SEEK, 20.0);

      VideoSyncDto expected = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.SEEK)
          .isPlaying(true)
          .timestamp(System.currentTimeMillis())
          .build();

      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      VideoSyncDto videoSyncDto = chatRoomService.updateVideoStatus(chatRoomId, request, userId);

      //then
      assertEquals(expected.videoControlAction(), videoSyncDto.videoControlAction());
      assertEquals(expected.isPlaying(), videoSyncDto.isPlaying());
      assertEquals(20.0, videoSyncDto.currentTime());

      verify(watchRoomRepository, times(1)).save(watchRoom);

    }

    @Test
    @DisplayName("지원하지 않는 비디오 제어")
    void failureWhenNotSupportedVideoControlAction() {

    }

    @Test
    @DisplayName("콘텐츠 길이 초과")
    void failureWhenCurrentTimeExceedContentLength(){

    }



  }
  @Nested
  @DisplayName("시청방 나가기")
  class leave{
    @Test
    @DisplayName("성공")
    void success() {

    }

  }
}
