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
  private WatchRoomServiceImpl watchRoomService;

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

    contentId = UUID.randomUUID();
    content = Content.builder()
        .title("테스트콘텐츠")
        .description("테스트용 콘텐츠 입니다.")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .build();
  }

  @Nested
  @DisplayName("채팅방 생성")
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
          .owner(user)
          .build();

      WatchRoomDto expected = WatchRoomDto.fromWatchRoomWithHeadcount(watchRoom, 1L);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      WatchRoomDto watchRoomDto = watchRoomService.create(request);

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
      assertThrows(ContentNotFoundException.class, () -> watchRoomService.create(request));

      verify(watchRoomRepository, never()).save(any(WatchRoom.class));
    }

  }

  @Nested
  @DisplayName("채팅방 전체 조회")
  class getAllWatchRoom {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID chatRoom1Id = UUID.randomUUID();
      WatchRoom watchRoom1 = WatchRoom.builder()
          .id(chatRoom1Id)
          .owner(user)
          .content(content)
          .build();

      UUID user2Id = UUID.randomUUID();
      User user2 = User.builder()
          .id(user2Id)
          .name("테스트유저2")
          .email("test@test.com")
          .password("test")
          .role(Role.USER)
          .build();
      UUID chatRoom2Id = UUID.randomUUID();
      WatchRoom watchRoom2 = WatchRoom.builder()
          .id(chatRoom2Id)
          .owner(user2)
          .content(content)
          .build();

      List<WatchRoom> watchRooms = List.of(watchRoom1, watchRoom2);

      List<WatchRoomContentWithHeadcountDto> queryResult = watchRooms.stream()
          .map(c -> new WatchRoomContentWithHeadcountDto(c, content, 1L))
          .toList();

      List<WatchRoomDto> expected = watchRooms.stream()
          .map(chatRoom -> WatchRoomDto.fromWatchRoomWithHeadcount(chatRoom, 1L))
          .toList();

      when(watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDto())
          .thenReturn(queryResult);

      //when
      List<WatchRoomDto> watchRoomDtos = watchRoomService.getAll();

      //then
      assertEquals(expected.size(), watchRoomDtos.size());
    }
  }

  @Nested
  @DisplayName("채팅방 개별 조회")
  class getWatchRoomById {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .owner(user)
          .content(content)
          .build();

      WatchRoomContentWithHeadcountDto queryResult
          = new WatchRoomContentWithHeadcountDto(watchRoom, content, 1L);

      WatchRoomDto expected = WatchRoomDto.fromWatchRoomWithHeadcount(watchRoom, 1L);

      when(watchRoomParticipantRepository.getWatchRoomContentWithHeadcountDto(chatRoomId))
          .thenReturn(Optional.of(queryResult));

      //when
      WatchRoomDto watchRoomDto = watchRoomService.getById(chatRoomId);

      //then
      assertEquals(expected.id(), watchRoomDto.id());
      assertEquals(expected.ownerId(), watchRoomDto.ownerId());
      assertEquals(expected.contentTitle(), watchRoomDto.contentTitle());
      assertEquals(expected.headCount(), watchRoomDto.headCount());

    }

    @Test
    @DisplayName("존재하지 않는 시청방")
    void failsWhenWatchRoomNotFound() {
      UUID randomId = UUID.randomUUID();

      when(watchRoomParticipantRepository.getWatchRoomContentWithHeadcountDto(randomId))
          .thenReturn(Optional.empty());

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class, () -> watchRoomService.getById(randomId));
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

      List<ParticipantDto> participantDtos = users.stream()
          .map(user -> {
            return ParticipantDto.builder()
                .username(user.getName())
                .profile(null)
                .isOwner(user.getId() == userId)
                .build();
          }).toList();

      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .owner(user)
          .content(content)
          .build();

      List<WatchRoomParticipant> watchRoomParticipant = users.stream().map(
          user -> {
            return WatchRoomParticipant.builder().user(user).watchRoom(watchRoom).build();
          }).toList();

      ParticipantsInfoDto participantsInfoDto = new ParticipantsInfoDto(participantDtos,
          participantDtos.size());

      WatchRoomInfoDto expected = WatchRoomInfoDto.builder()
          .id(chatRoomId)
          .contentTitle(watchRoom.getContent().getTitle())
          .participantsInfoDto(participantsInfoDto)
          .build();

      when(userRepository.findByEmail(participant.getEmail())).thenReturn(Optional.of(participant));
      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository
          .existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, participant))
          .thenReturn(false);
      when(watchRoomParticipantRepository.findByWatchRoom(watchRoom))
          .thenReturn(watchRoomParticipant);

      //when
      WatchRoomInfoDto watchRoomInfoDto = watchRoomService
          .joinWatchRoomAndGetInfo(chatRoomId, participant.getEmail());

      assertEquals(expected.id(), watchRoomInfoDto.id());
      assertEquals(expected.contentTitle(), watchRoomInfoDto.contentTitle());
      assertEquals(expected.participantsInfoDto().participantsCount(),
          watchRoomInfoDto.participantsInfoDto().participantsCount());

      verify(watchRoomParticipantRepository, times(1))
          .save(any(WatchRoomParticipant.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      //given
      String randomEmail = "random@test.com";

      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .owner(user)
          .content(content)
          .build();

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> watchRoomService.joinWatchRoomAndGetInfo(chatRoomId, randomEmail));

      verify(watchRoomParticipantRepository, never()).save(any(WatchRoomParticipant.class));
    }

    @Test
    @DisplayName("존재하지 않는 채팅방")
    void failsWhenWatchRoomNotFound() {
      //given
      String participantEmail = "participant@test.com";

      UUID randomId = UUID.randomUUID();

      when(userRepository.findByEmail(participantEmail)).thenReturn(Optional.of(user));
      when(watchRoomRepository.findById(randomId)).thenReturn(Optional.empty());

      //when & then
      assertThrows(WatchRoomRoomNotFoundException.class,
          () -> watchRoomService.joinWatchRoomAndGetInfo(randomId, participantEmail));

      verify(watchRoomParticipantRepository, never()).save(any(WatchRoomParticipant.class));
    }
  }

  @Nested
  @DisplayName("시청방 유저 목록 조회")
  class getParticipants {

    @Test
    @DisplayName("성공")
    void success() {
      UUID watchRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(watchRoomId)
          .owner(user)
          .content(content)
          .build();

      WatchRoomParticipant watchRoomParticipantOwner = WatchRoomParticipant.builder()
          .user(user)
          .watchRoom(watchRoom)
          .build();

      List<WatchRoomParticipant> watchRoomParticipants = List.of(watchRoomParticipantOwner);

      when(watchRoomRepository.findById(watchRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository.findByWatchRoom(watchRoom))
          .thenReturn(watchRoomParticipants);

      // when
      ParticipantsInfoDto participants = watchRoomService.getParticipants(watchRoomId);

      // then
      assertNotNull(participants);
      assertEquals(1, participants.participantsCount());
    }

    @Test
    @DisplayName("존재하지 않는 시청방")
    void failureWhenWatchRoomNotFound() {
      //given
      UUID randomUUID = UUID.randomUUID();

      when(watchRoomRepository.findById(randomUUID)).thenReturn(Optional.empty());

      // when & then
      assertThrows(WatchRoomRoomNotFoundException.class,  ()-> watchRoomService.getParticipants(randomUUID));

      verify(watchRoomParticipantRepository, never()).findByWatchRoom(any(WatchRoom.class));
    }
  }

  @Nested
  @DisplayName("시청방 비디오 제어")
  class updateVideoStatus {

    @Test
    @DisplayName("일시 정지 성공")
    void successPause() {
      //given
      UUID chatRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(chatRoomId)
          .owner(user)

          .content(content)
          .videoStateUpdatedAt(LocalDateTime.now())
          .isPlaying(true)
          .playTime(10.0)
          .build();

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.PAUSE, 10.0);

      VideoSyncDto expected = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.PAUSE)
          .isPlaying(false)
          .timestamp(System.currentTimeMillis())
          .build();

      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      VideoSyncDto videoSyncDto = watchRoomService.updateVideoStatus(chatRoomId, request,
          user.getEmail());

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
          .owner(user)

          .content(content)
          .videoStateUpdatedAt(LocalDateTime.now())
          .isPlaying(false)
          .playTime(10.0)
          .build();

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.PLAY, 10.0);

      VideoSyncDto expected = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.PLAY)
          .isPlaying(true)
          .timestamp(System.currentTimeMillis())
          .build();

      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      VideoSyncDto videoSyncDto = watchRoomService
          .updateVideoStatus(chatRoomId, request, user.getEmail());

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
          .owner(user)

          .content(content)
          .videoStateUpdatedAt(LocalDateTime.now())
          .isPlaying(true)
          .playTime(10.0)
          .build();

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.SEEK, 20.0);

      VideoSyncDto expected = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.SEEK)
          .isPlaying(true)
          .timestamp(System.currentTimeMillis())
          .build();

      when(watchRoomRepository.findById(chatRoomId)).thenReturn(Optional.of(watchRoom));
      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(watchRoomRepository.save(any(WatchRoom.class))).thenReturn(watchRoom);

      //when
      VideoSyncDto videoSyncDto = watchRoomService.updateVideoStatus(chatRoomId, request,
          user.getEmail());

      //then
      assertEquals(expected.videoControlAction(), videoSyncDto.videoControlAction());
      assertEquals(expected.isPlaying(), videoSyncDto.isPlaying());
      assertEquals(20.0, videoSyncDto.currentTime());

      verify(watchRoomRepository, times(1)).save(watchRoom);

    }

    @Test
    @DisplayName("지원하지 않는 비디오 제어")
    void failureWhenNotSupportedVideoControlAction() {
      //todo
    }

    @Test
    @DisplayName("콘텐츠 길이 초과")
    void failureWhenCurrentTimeExceedContentLength() {
      //todo
    }
  }

  @Nested
  @DisplayName("시청방 나가기")
  class leave {

    @Test
    @DisplayName("방장이 나가면 방장 권한은 다른 참여자에게 넘어감")
    void successWhenOwnerLeave() {
      // given
      UUID watchRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(watchRoomId)
          .owner(user)
          .content(content)
          .build();

      UUID participantId = UUID.randomUUID();
      User participant = User.builder()
          .id(participantId)
          .email("participant@test.com")
          .name("participant")
          .build();

      WatchRoomParticipant watchRoomParticipantOwner = WatchRoomParticipant.builder()
          .user(user)
          .watchRoom(watchRoom)
          .build();

      WatchRoomParticipant watchRoomParticipant = WatchRoomParticipant.builder()
          .user(participant)
          .watchRoom(watchRoom)
          .build();

      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(watchRoomRepository.findById(watchRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository.findByUserAndWatchRoom(user, watchRoom))
          .thenReturn(Optional.of(watchRoomParticipantOwner));
      when(watchRoomParticipantRepository.findFirstByWatchRoom(watchRoom))
          .thenReturn(Optional.of(watchRoomParticipant));

      // when
      watchRoomService.leave(watchRoomId, user.getEmail());

      // then
      assertEquals(watchRoomId, watchRoom.getId());
      assertEquals(participant.getEmail(), watchRoom.getOwner().getEmail());
    }

    @Test
    @DisplayName("모든 참여자가 나가면 방이 삭제됨")
    void successWhenOwnerIsTheLastOne() {
      // given
      UUID watchRoomId = UUID.randomUUID();
      WatchRoom watchRoom = WatchRoom.builder()
          .id(watchRoomId)
          .owner(user)
          .content(content)
          .build();

      WatchRoomParticipant watchRoomParticipantOwner = WatchRoomParticipant.builder()
          .user(user)
          .watchRoom(watchRoom)
          .build();

      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(watchRoomRepository.findById(watchRoomId)).thenReturn(Optional.of(watchRoom));
      when(watchRoomParticipantRepository.findByUserAndWatchRoom(user, watchRoom))
          .thenReturn(Optional.of(watchRoomParticipantOwner));
      when(watchRoomParticipantRepository.findFirstByWatchRoom(watchRoom))
          .thenReturn(Optional.empty());

      // when
      watchRoomService.leave(watchRoomId, user.getEmail());

      // then
      verify(watchRoomRepository, times(1)).delete(watchRoom);
    }

    @Test
    @DisplayName("해당 이메일을 가진 유저가 없음")
    void failureWhenUserEmailNotFound() {
      // given
      String randomEmail = "notFound@test.com";

      UUID watchRoomId = UUID.randomUUID();

      when(userRepository.findByEmail(randomEmail)).thenReturn(Optional.empty());

      //when & then
      assertThrows(UserNotFoundException.class,
          () -> watchRoomService.leave(watchRoomId, randomEmail));

    }

    @Test
    @DisplayName("해당 ID를 가진 시청방이 없음")
    void failureWhenIdNotFound() {
      //given
      UUID randomUUID = UUID.randomUUID();

      when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
      when(watchRoomRepository.findById(randomUUID)).thenReturn(Optional.empty());

      // when & then
      assertThrows(WatchRoomRoomNotFoundException.class,
          () -> watchRoomService.leave(randomUUID, user.getEmail()));
    }
  }
}
