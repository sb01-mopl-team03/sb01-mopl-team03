package team03.mopl.domain.watchroom.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
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

@Service
@RequiredArgsConstructor
public class WatchRoomServiceImpl implements WatchRoomService {

  private final UserRepository userRepository;
  private final ContentRepository contentRepository;
  private final WatchRoomParticipantRepository watchRoomParticipantRepository;
  private final WatchRoomRepository watchRoomRepository;
  private final ObjectMapper objectMapper;

  @Override
  @Transactional
  public WatchRoomDto create(WatchRoomCreateRequest request) {

    User owner = userRepository.findById(request.ownerId())
        .orElseThrow(UserNotFoundException::new);

    Content content = contentRepository.findById(request.contentId()).orElseThrow(
        ContentNotFoundException::new);

    WatchRoom watchRoom = WatchRoom.builder()
        .owner(owner)
        .content(content)
        .build();

    watchRoom = watchRoomRepository.save(watchRoom);

    WatchRoomParticipant watchRoomParticipant = WatchRoomParticipant.builder()
        .user(owner)
        .watchRoom(watchRoom)
        .build();

    watchRoomParticipantRepository.save(watchRoomParticipant);

    return WatchRoomDto.fromWatchRoomWithHeadcount(watchRoom, 1);
  }



  @Override
  public CursorPageResponseDto<WatchRoomDto> getAll(WatchRoomSearchDto request) {
    Cursor cursor = decodeCursor(request.getCursor());

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
    WatchRoomDto nextCursor =result.isEmpty()? null : result.get(result.size() - 1);

    //총 개수
    long totalElements = watchRoomParticipantRepository
        .countWatchRoomContentWithHeadcountDto(request.getSearchKeyword());

    return CursorPageResponseDto.<WatchRoomDto>builder()
        .data(result)
        .nextCursor(nextCursor == null? null : encodeNextCursor(nextCursor, request.getSortBy()))
        .hasNext(hasNext)
        .totalElements(totalElements)
        .size(result.size())
        .build();
  }

  // 커서 디코더
  private Cursor decodeCursor(String encodedCursor) {
    if (encodedCursor == null) {
      return new Cursor(null, null);
    }

    try {
      byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedCursor);
      String decodedJson = new String(decodedBytes, StandardCharsets.UTF_8);
      return objectMapper.readValue(decodedJson, Cursor.class);
    } catch (Exception e) {
      return new Cursor(null, null);
    }
  }

  // 커서 인코딩
  public String encodeNextCursor(WatchRoomDto lastItem, String sortBy) {
    String lastId = lastItem.id().toString();
    String lastValue = extractLastValue(lastItem, sortBy);

    Cursor cursor = new Cursor(lastValue, lastId);
    try {
      String cursorToJson = objectMapper.writeValueAsString(cursor);
      return Base64.getUrlEncoder().encodeToString(cursorToJson.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException("커서 인코딩 실패", e);
    }
  }

  private String extractLastValue(WatchRoomDto lastItem, String sortBy) {
    String lowerSortBy = sortBy == null? "participantcount" : sortBy.toLowerCase();

    return switch (lowerSortBy) {
      case "createdat" -> lastItem.createdAt().toString();
      case "title" -> lastItem.title();
      case "participantcount" -> String.valueOf(lastItem.headCount());
      default -> throw new IllegalArgumentException("지원하지 않는 정렬 방식: " + sortBy);
    };
  }

  @Override
  @Transactional(readOnly = true)
  public WatchRoomDto getById(UUID watchRoomId) {
    WatchRoomContentWithParticipantCountDto watchRoomContentWithParticipantCountDto
        = watchRoomParticipantRepository.getWatchRoomContentWithHeadcountDto(watchRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);
    return WatchRoomDto.from(watchRoomContentWithParticipantCountDto);
  }

  @Override
  @Transactional
  public WatchRoomInfoDto joinWatchRoomAndGetInfo(UUID chatRoomId, String username) {
    User user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(chatRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    if (watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(watchRoom,
        user)) {
      return getWatchRoomInfoDtoWithNewUser(watchRoom, user);
    }

    WatchRoomParticipant watchRoomParticipant = WatchRoomParticipant.builder()
        .watchRoom(watchRoom)
        .user(user)
        .build();

    watchRoomParticipantRepository.save(watchRoomParticipant);

    return getWatchRoomInfoDtoWithNewUser(watchRoom, user);
  }

  @Override
  @Transactional
  public VideoSyncDto updateVideoStatus(UUID roomId, VideoControlRequest request, String username) {
    WatchRoom watchRoom = watchRoomRepository.findById(roomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    User user = userRepository.findByEmail(username).orElseThrow(UserNotFoundException::new);

    //방장이 아니라면 제어 권한 없음
    if (!watchRoom.getOwner().getId().equals(user.getId())) {
      throw new IllegalArgumentException("방장이 아님");
    }

    switch (request.videoControlAction()) {
      case PLAY -> watchRoom.play();
      case PAUSE -> watchRoom.pause();
      case SEEK -> watchRoom.seekTo(request.currentTime());
      default -> throw new IllegalArgumentException("지원하지 않는 비디오 제어");
    }

    WatchRoom savedWatchRoom = watchRoomRepository.save(watchRoom);

    return VideoSyncDto.builder()
        .videoControlAction(request.videoControlAction())
        .currentTime(savedWatchRoom.getPlayTime())
        .isPlaying(savedWatchRoom.getIsPlaying())
        .build();
  }

  @Override
  @Transactional
  public ParticipantsInfoDto getParticipants(UUID roomId) {

    WatchRoom watchRoom = watchRoomRepository.findById(roomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    return getParticipantsInfoDto(watchRoom);
  }

  @Override
  @Transactional(readOnly = true)
  public WatchRoomInfoDto getWatchRoomInfo(UUID chatRoomId) {
    WatchRoom watchRoom = watchRoomRepository.findById(chatRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    return getWatchRoomInfoDto(watchRoom);
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
    return WatchRoomInfoDto.builder()
        .id(watchRoom.getId())
        .newUserId(user.getId())
        .contentTitle(watchRoom.getContent().getTitle())
        .contentUrl(watchRoom.getContent().getYoutubeUrl())
        .participantsInfoDto(getParticipantsInfoDto(watchRoom))
        .build();
  }

  //시청방 정보 + 참여자 정보 조회
  private WatchRoomInfoDto getWatchRoomInfoDto(WatchRoom watchRoom) {

    return WatchRoomInfoDto.builder()
        .id(watchRoom.getId())
        .contentTitle(watchRoom.getContent().getTitle())
        .contentUrl(watchRoom.getContent().getYoutubeUrl())
        .participantsInfoDto(getParticipantsInfoDto(watchRoom))
        .build();
  }

  // 참여자 목록 조회
// todo - 리팩토링
  private ParticipantsInfoDto getParticipantsInfoDto(WatchRoom watchRoom) {
    List<WatchRoomParticipant> participants = watchRoomParticipantRepository.findByWatchRoom(
        watchRoom);

    List<ParticipantDto> participantList = participants.stream()
        .map(participant -> new ParticipantDto(
            participant.getUser().getName(),
            participant.getUser().getProfileImage(),
            participant.getUser().getId().equals(watchRoom.getOwner().getId()))).toList();

    return ParticipantsInfoDto.builder()
        .participantDtoList(participantList)
        .participantCount(participantList.size())
        .build();
  }
}
