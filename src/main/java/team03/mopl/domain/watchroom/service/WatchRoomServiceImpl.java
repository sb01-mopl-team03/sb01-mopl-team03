package team03.mopl.domain.watchroom.service;


import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;
import team03.mopl.domain.watchroom.dto.ParticipantDto;
import team03.mopl.domain.watchroom.dto.ParticipantsInfoDto;
import team03.mopl.domain.watchroom.dto.VideoControlRequest;
import team03.mopl.domain.watchroom.dto.VideoSyncDto;
import team03.mopl.domain.watchroom.dto.WatchRoomInfoDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomParticipant;
import team03.mopl.domain.watchroom.exception.AlreadyJoinedWatchRoomRoomException;
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

  @Override
  @Transactional
  public WatchRoomDto create(WatchRoomCreateRequest request) {

    User owner = userRepository.findById(request.ownerId())
        .orElseThrow(UserNotFoundException::new);

    Content content = contentRepository.findById(request.contentId()).orElseThrow(
        ContentNotFoundException::new);

    WatchRoom watchRoom = WatchRoom.builder()
        .ownerId(owner.getId())
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
  @Transactional(readOnly = true)
  public List<WatchRoomDto> getAll() {
    return watchRoomParticipantRepository.getAllWatchRoomContentWithHeadcountDto()
        .stream()
        .map(WatchRoomDto::from).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public WatchRoomDto getById(UUID id) {
    //todo - 개선
    WatchRoom watchRoom = watchRoomRepository.findById(id).orElseThrow(
        WatchRoomRoomNotFoundException::new);
    watchRoomParticipantRepository.countByWatchRoomId(watchRoom.getId());
    return WatchRoomDto.fromWatchRoomWithHeadcount(watchRoom, 1);
  }

  @Override
  @Transactional
  public WatchRoomInfoDto joinWatchRoomAndGetInfo(UUID chatRoomId, UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(chatRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    if (watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, user)) {
      throw new AlreadyJoinedWatchRoomRoomException();
    }

    WatchRoomParticipant watchRoomParticipant = WatchRoomParticipant.builder()
        .watchRoom(watchRoom)
        .user(user)
        .build();

    watchRoomParticipantRepository.save(watchRoomParticipant);

    return getWatchRoomInfoDto(watchRoom);

  }

  @Override
  @Transactional
  public VideoSyncDto updateVideoStatus(UUID roomId, VideoControlRequest request, UUID requesterId) {
    WatchRoom watchRoom = watchRoomRepository.findById(roomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    //방장이 아니라면 제어 권한 없음
    if(!watchRoom.getOwnerId().equals(requesterId)) {
      throw new IllegalArgumentException("방장이 아님");
    }

    switch (request.videoControlAction()){
      case PLAY -> watchRoom.play();
      case PAUSE -> watchRoom.pause();
      case SEEK -> watchRoom.seekTo(request.currentTime());
      default -> throw new IllegalArgumentException("지원하지 않는 비디오 제어");
    }

    WatchRoom savedWatchRoom = watchRoomRepository.save(watchRoom);

    return VideoSyncDto.builder()
        .videoControlAction(request.videoControlAction())
        .currentTime(savedWatchRoom.getCurrentTime())
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
  public void leave(UUID roomId, UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(roomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    watchRoomParticipantRepository.findByUserAndWatchRoom(user, watchRoom)
        .ifPresent(watchRoomParticipantRepository::delete);
  }

  //시청방 정보 + 참여자 정보 조회
  private WatchRoomInfoDto getWatchRoomInfoDto(WatchRoom watchRoom) {

    return WatchRoomInfoDto.builder()
        .id(watchRoom.getId())
        .contentTitle(watchRoom.getContent().getTitle())
        .participantsInfoDto(getParticipantsInfoDto(watchRoom))
        .build();
  }

  // 참여자 목록 조회
  private ParticipantsInfoDto getParticipantsInfoDto(WatchRoom watchRoom) {
    List<WatchRoomParticipant> participants = watchRoomParticipantRepository.findByWatchRoom(watchRoom);

    List<ParticipantDto> participantList = participants.stream()
        .map(participant -> new ParticipantDto(
            participant.getUser().getName(),
            null,
            //todo - 프로필 필드 추가 시 변경
            //participant.getUser().getProfile(),
            participant.getUser().getId().equals(watchRoom.getOwnerId()))).toList();

    return ParticipantsInfoDto.builder()
        .participantDtoList(participantList)
        .participantsCount(participantList.size())
        .build();
  }
}
