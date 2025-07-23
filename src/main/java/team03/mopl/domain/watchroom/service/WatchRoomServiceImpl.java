package team03.mopl.domain.watchroom.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.common.util.CursorCodecUtil;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithParticipantCountDto;
import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchInternalDto;
import team03.mopl.domain.watchroom.dto.participant.ParticipantDto;
import team03.mopl.domain.watchroom.dto.participant.ParticipantsInfoDto;
import team03.mopl.domain.watchroom.dto.video.VideoControlRequest;
import team03.mopl.domain.watchroom.dto.video.VideoSyncDto;
import team03.mopl.domain.watchroom.dto.WatchRoomInfoDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomParticipant;
import team03.mopl.domain.watchroom.exception.WatchRoomRoomNotFoundException;
import team03.mopl.domain.watchroom.repository.WatchRoomParticipantRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomRepository;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchRoomServiceImpl implements WatchRoomService {

  private final UserRepository userRepository;
  private final ContentRepository contentRepository;
  private final WatchRoomParticipantRepository watchRoomParticipantRepository;
  private final WatchRoomRepository watchRoomRepository;
  //
  private final CursorCodecUtil codecUtil;

  @Override
  @Transactional
  public WatchRoomDto create(WatchRoomCreateRequest request) {
    log.info("create - 실시간 시청방 생성 시작: userId = {}, contentId = {}, title = {}",
        request.ownerId(), request.contentId(), request.title());

    User owner = userRepository.findById(request.ownerId())
        .orElseThrow(UserNotFoundException::new);

    Content content = contentRepository.findById(request.contentId()).orElseThrow(
        ContentNotFoundException::new);

    WatchRoom watchRoom = WatchRoom.builder()
        .title(request.title())
        .owner(owner)
        .content(content)
        .build();

    watchRoom = watchRoomRepository.save(watchRoom);

    WatchRoomParticipant watchRoomParticipant = WatchRoomParticipant.builder()
        .user(owner)
        .watchRoom(watchRoom)
        .build();

    watchRoomParticipantRepository.save(watchRoomParticipant);

    log.info("create - 실시간 시청방 생성 완료: watchRoomId = {}, ownerId = {}, contentId = {}, title = {}",
        watchRoom.getId(), watchRoom.getOwner().getId(), watchRoom.getContent().getId(),
        watchRoom.getTitle());

    return WatchRoomDto.fromWatchRoomWithHeadcount(watchRoom, 1);
  }


  @Override
  public CursorPageResponseDto<WatchRoomDto> getAll(WatchRoomSearchDto request) {
    log.info("getAll - 실시간 시청방 페이지네이션 조회 시작");
    log.debug("searchKeyword = {}, sortBy = {}, direction = {}, cursor = {}, size = {}",
        request.getSearchKeyword(), request.getSortBy(), request.getDirection(),
        request.getCursor(), request.getSize());

    Cursor cursor = codecUtil.decodeCursor(request.getCursor());

    WatchRoomSearchInternalDto watchRoomSearchInternalDto =
        WatchRoomSearchInternalDto.fromRequestWithCursor(request, cursor);

    // 결과
    List<WatchRoomDto> result = new ArrayList<>(watchRoomParticipantRepository
        .getAllWatchRoomContentWithHeadcountDtoPaginated(watchRoomSearchInternalDto)
        .stream().map(WatchRoomDto::from).toList());

    // 다음 페이지 있는지 검사
    boolean hasNext = result.size() > watchRoomSearchInternalDto.getSize();

    // 비어있지 않고, 다음 게 있다면 결과의 마지막을 삭제
    if (!result.isEmpty() && hasNext) {
      result.remove(result.size() - 1);
    }

    //다음 커서
    WatchRoomDto nextCursor = result.isEmpty() ? null : result.get(result.size() - 1);

    //총 개수
    long totalElements = watchRoomParticipantRepository
        .countWatchRoomContentWithHeadcountDto(request.getSearchKeyword());

    log.info("getAll - 실시간 시청방 페이지네이션 조회 완료: 전체 검색 결과 수 = {}", totalElements);
    return CursorPageResponseDto.<WatchRoomDto>builder()
        .data(result)
        .nextCursor(nextCursor == null? null : codecUtil.encodeNextCursor(nextCursor,
            request.getSortBy()))
        .hasNext(hasNext)
        .totalElements(totalElements)
        .size(result.size())
        .build();
  }


  @Override
  @Transactional(readOnly = true)
  public WatchRoomDto getById(UUID watchRoomId) {
    log.info("getById - 실시간 시청방 단일 조회 시작: watchRoomId = {}", watchRoomId);

    WatchRoomContentWithParticipantCountDto watchRoomContentWithParticipantCountDto
        = watchRoomParticipantRepository.getWatchRoomContentWithHeadcountDto(watchRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    log.info("getById - 실시간 시청방 단일 조회 완료: watchRoomId = {}, participantCount = {}",
        watchRoomId, watchRoomContentWithParticipantCountDto.getParticipantCount());
    return WatchRoomDto.from(watchRoomContentWithParticipantCountDto);
  }

  @Override
  @Transactional
  public WatchRoomInfoDto joinWatchRoomAndGetInfo(UUID watchRoomId, String userEmail) {
    log.info("joinWatchRoomAndGetInfo - 실시간 시청방 참여 시작: watchRoomId = {}, userEmail = {}",
        watchRoomId, userEmail);

    User user = userRepository.findByEmail(userEmail).orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(watchRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    if (watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(watchRoom,
        user)) {
      log.warn("이미 시청방에 참여 중인 사용자");
      return getWatchRoomInfoDtoWithNewUser(watchRoom, user);
    }

    WatchRoomParticipant watchRoomParticipant = WatchRoomParticipant.builder()
        .watchRoom(watchRoom)
        .user(user)
        .build();

    WatchRoomParticipant saved = watchRoomParticipantRepository.save(watchRoomParticipant);

    log.info("joinWatchRoomAndGetInfo - 실시간 시청방 참여 완료: watchRoomId = {}, userId = {},"
        + " watchRoomParticipantId = {}", watchRoomId, user.getId(), saved.getId());

    return getWatchRoomInfoDtoWithNewUser(watchRoom, user);
  }

  @Override
  @Transactional
  public VideoSyncDto updateVideoStatus(UUID roomId, VideoControlRequest request, String username) {
    log.info("updateVideoStatus - 비디오 상태 업데이트 시작: roomId = {}, username = {}, "
            + "videoControlAction = {}, playTime = {}", roomId, username, request.videoControlAction(),
        request.currentTime());

    WatchRoom watchRoom = watchRoomRepository.findById(roomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    User user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);

    //방장이 아니라면 제어 권한 없음
    if (!watchRoom.getOwner().getId().equals(user.getId())) {
      log.warn("방장이 아닌 사람이 제어 시도");
      throw new IllegalArgumentException("방장이 아님");
    }

    switch (request.videoControlAction()) {
      case PLAY -> watchRoom.play();
      case PAUSE -> watchRoom.pause(request.currentTime());
      case SEEK -> watchRoom.seekTo(request.currentTime());
      default -> throw new IllegalArgumentException("지원하지 않는 비디오 제어");
    }

    WatchRoom savedWatchRoom = watchRoomRepository.save(watchRoom);

    log.info("updateVideoStatus - 비디오 상태 업데이트 완료: roomId = {}, username = {}, isPlaying = {},"
            + "playTime = {}", savedWatchRoom.getId(), savedWatchRoom.getOwner().getId(),
        savedWatchRoom.getIsPlaying(), savedWatchRoom.getPlayTime());

    return VideoSyncDto.builder()
        .videoControlAction(request.videoControlAction())
        .currentTime(savedWatchRoom.getPlayTime())
        .isPlaying(savedWatchRoom.getIsPlaying())
        .build();
  }

  @Override
  @Transactional
  public ParticipantsInfoDto getParticipants(UUID watchRoomId) {
    log.info("getParticipants - 참여자 정보 조회 시작: watchRoomId = {}", watchRoomId);

    WatchRoom watchRoom = watchRoomRepository.findById(watchRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    ParticipantsInfoDto participantsInfoDto = getParticipantsInfoDto(watchRoom);
    log.info("getParticipants - 참여자 정보 조회 완료: watchRoomId = {}, participantCount = {}",
        watchRoomId, participantsInfoDto.participantCount());
    return participantsInfoDto;
  }

  @Override
  @Transactional(readOnly = true)
  public WatchRoomInfoDto getWatchRoomInfo(UUID watchRoomId) {
    log.info("getWatchRoomInfo - 실시간 시청방 단일 조회 시작: watchRoomId = {}", watchRoomId);
    WatchRoom watchRoom = watchRoomRepository.findById(watchRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    WatchRoomInfoDto watchRoomInfoDto = getWatchRoomInfoDto(watchRoom);
    log.info("getWatchRoomInfo - 실시간 시청방 단일 조회 완료: watchRoomId = {}", watchRoomId);
    return watchRoomInfoDto;
  }

  @Override
  @Transactional
  public void leave(UUID roomId, String username) {
    User user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(roomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    watchRoomParticipantRepository.findByUserAndWatchRoom(user, watchRoom)
        .ifPresent(watchRoomParticipantRepository::delete);

    watchRoomParticipantRepository.findFirstByWatchRoom(watchRoom).ifPresentOrElse(
        // 남아있는 사람이 있다면 참여자 중 한명에게 방장 넘김
        watchRoomParticipant -> {
          User newOwner = watchRoomParticipant.getUser();
          watchRoom.changeOwner(newOwner);
        },
        // 남아있는 사람이 아무도 없으면 시청방 삭제
        () -> {
          watchRoomRepository.delete(watchRoom);
        }
    );
  }

  private WatchRoomInfoDto getWatchRoomInfoDtoWithNewUser(WatchRoom watchRoom, User user) {
    log.debug("getWatchRoomInfoDtoWithNewUser - 시청방 정보 조회 및 새 참여자: watchRoomId = {}", watchRoom.getId());

    return WatchRoomInfoDto.builder()
        .id(watchRoom.getId())
        .newUserId(user.getId())
        .playTime(watchRoom.getPlayTime())
        .isPlaying(watchRoom.getIsPlaying())
        .content(ContentDto.from(watchRoom.getContent()))
        .participantsInfoDto(getParticipantsInfoDto(watchRoom))
        .build();
  }

  //시청방 정보 + 참여자 정보 조회
  private WatchRoomInfoDto getWatchRoomInfoDto(WatchRoom watchRoom) {
    log.debug("getWatchRoomInfoDto - 시청방 정보 조회: watchRoomId = {}", watchRoom.getId());
    WatchRoomInfoDto watchRoomInfoDto = WatchRoomInfoDto.builder()
        .id(watchRoom.getId())
        .content(ContentDto.from(watchRoom.getContent()))
        .playTime(watchRoom.getPlayTime())
        .isPlaying(watchRoom.getIsPlaying())
        .participantsInfoDto(getParticipantsInfoDto(watchRoom))
        .build();

    log.debug(
        "getWatchRoomInfoDto - 시청방 정보 조회 결과: watchRoomId = {}, contentId = {}, participantCount = {}",
        watchRoom.getId(), watchRoomInfoDto.content().id(),
        watchRoomInfoDto.participantsInfoDto().participantCount());

    return watchRoomInfoDto;
  }

  // 참여자 목록 조회
// todo - 리팩토링
  private ParticipantsInfoDto getParticipantsInfoDto(WatchRoom watchRoom) {
    log.debug("getParticipantsInfoDto - 참여자 목록 조회: watchRoomId = {}", watchRoom.getId());
    List<WatchRoomParticipant> participants = watchRoomParticipantRepository.findByWatchRoom(
        watchRoom);

    List<ParticipantDto> participantList = participants.stream()
        .map(participant -> new ParticipantDto(
            participant.getUser().getId(),
            participant.getUser().getName(),
            participant.getUser().getProfileImage(),
            participant.getUser().getId().equals(watchRoom.getOwner().getId()))).toList();

    log.debug("getParticipantsInfoDto - 참여자 목록 조회 결과: watchRoomId = {}, participantCount = {} ",
        watchRoom.getId(), participants.size());
    return ParticipantsInfoDto.builder()
        .participantDtoList(participantList)
        .participantCount(participantList.size())
        .build();
  }
}
