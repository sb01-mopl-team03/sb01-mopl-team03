package team03.mopl.domain.watchroom.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageDto;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomMessage;
import team03.mopl.domain.watchroom.exception.WatchRoomRoomNotFoundException;
import team03.mopl.domain.watchroom.repository.WatchRoomMessageRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomParticipantRepository;
import team03.mopl.domain.watchroom.repository.WatchRoomRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class WatchRoomMessageServiceImpl implements WatchRoomMessageService {

  private final WatchRoomMessageRepository watchRoomMessageRepository;
  private final WatchRoomRepository watchRoomRepository;
  private final WatchRoomParticipantRepository watchRoomParticipantRepository;
  private final UserRepository userRepository;

  @Override
  public WatchRoomMessageDto create(WatchRoomMessageCreateRequest request, String userEmail) {

    User sender = userRepository.findByEmail(userEmail)
        .orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(request.chatRoomId())
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    if (!watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, sender))
    {
      throw new WatchRoomRoomNotFoundException();
    }

    WatchRoomMessage watchRoomMessage = WatchRoomMessage.builder()
        .sender(sender)
        .watchRoom(watchRoom)
        .content(request.content())
        .build();

    return WatchRoomMessageDto.from(watchRoomMessageRepository.save(watchRoomMessage));
  }

  @Override
  public List<WatchRoomMessageDto> getAllByRoomId(UUID chatRoomId, UUID userId) {

    //todo - userId를 이후에 context에서 가져올 수 있게되면 userId 파라미터 제거하여 리팩토링

    User sender = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    WatchRoom watchRoom = watchRoomRepository.findById(chatRoomId)
        .orElseThrow(WatchRoomRoomNotFoundException::new);

    if (!watchRoomParticipantRepository.existsWatchRoomParticipantByWatchRoomAndUser(watchRoom, sender))
    {
      throw new WatchRoomRoomNotFoundException();
    }

    List<WatchRoomMessage> watchRoomMessages = watchRoomMessageRepository.findAllByWatchRoom(watchRoom);

    return watchRoomMessages.stream().map(WatchRoomMessageDto::from).toList();
  }
}
