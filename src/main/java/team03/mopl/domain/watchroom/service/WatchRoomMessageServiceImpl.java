package team03.mopl.domain.watchroom.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.watchroom.dto.watchroommessage.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.watchroommessage.WatchRoomMessageDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomMessage;
import team03.mopl.common.exception.watchroom.WatchRoomRoomNotFoundException;
import team03.mopl.domain.watchroom.repository.WatchRoomMessageRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomParticipantRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WatchRoomMessageServiceImpl implements WatchRoomMessageService {

  private final WatchRoomMessageRepository watchRoomMessageRepository;
  private final WatchRoomRepository watchRoomRepository;
  private final WatchRoomParticipantRepository watchRoomParticipantRepository;
  private final UserRepository userRepository;

  @Override
  public WatchRoomMessageDto create(WatchRoomMessageCreateRequest request, String userEmail) {
    //todo - refactor(파라미터명 변경)
    log.info("create - 실시간 시청방 메세지 생성 시작: watchRoomId = {}, userEmail = {}",
        request.chatRoomId(), userEmail);

    User sender = userRepository.findByEmail(userEmail)
        .orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(request.chatRoomId())
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    if (!watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(watchRoom,
        sender)) {
      log.warn("해당 실시간 시청방에 참여하지 않은 사용자");
      throw new WatchRoomRoomNotFoundException();
    }

    WatchRoomMessage watchRoomMessage = WatchRoomMessage.builder()
        .sender(sender)
        .watchRoom(watchRoom)
        .content(request.content())
        .build();

    WatchRoomMessage saved = watchRoomMessageRepository.save(watchRoomMessage);

    log.info("create - 실시간 시청방 메세지 생성 완료: watchRoomMessageId = {}, watchRoomId = {} ,senderId = {}",
        saved.getId(), saved.getWatchRoom().getId(), saved.getSender().getId());

    return WatchRoomMessageDto.from(saved);
  }

  @Override
  public List<WatchRoomMessageDto> getAllByRoomId(UUID watchRoomId, UUID userId) {
    log.info("getAllByRoomId - 실시간 시청방 메세지 전체 조회 시작: watchRoomId = {}, userId = {}",
        watchRoomId, userId);

    //todo - userId를 이후에 context에서 가져올 수 있게되면 userId 파라미터 제거하여 리팩토링

    User sender = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(watchRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    if (!watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(watchRoom,
        sender)) {
      throw new WatchRoomRoomNotFoundException();
    }

    List<WatchRoomMessage> watchRoomMessages = watchRoomMessageRepository.findAllByWatchRoom(
        watchRoom);

    log.info("getAllByRoomId - 실시간 시청방 메세지 전체 조회 완료 : watchRoomId = {}, 총 메세지 수 = {}",
        watchRoomId, watchRoomMessages.size());
    return watchRoomMessages.stream().map(WatchRoomMessageDto::from).toList();
  }
}
