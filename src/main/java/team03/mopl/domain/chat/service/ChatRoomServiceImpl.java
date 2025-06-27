package team03.mopl.domain.chat.service;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.chat.dto.ChatRoomCreateRequest;
import team03.mopl.domain.chat.dto.ChatRoomDto;
import team03.mopl.domain.chat.entity.ChatRoom;
import team03.mopl.domain.chat.entity.ChatRoomParticipant;
import team03.mopl.domain.chat.repository.ChatRoomParticipantRepository;
import team03.mopl.domain.chat.repository.ChatRoomRepository;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

  private final UserRepository userRepository;
  private final ContentRepository contentRepository;
  private final ChatRoomParticipantRepository chatRoomParticipantRepository;
  private final ChatRoomRepository chatRoomRepository;

  @Override
  @Transactional
  public ChatRoomDto create(ChatRoomCreateRequest request) {

    User owner = userRepository.findById(request.ownerId())
        .orElseThrow(UserNotFoundException::new);

    ChatRoom chatRoom = ChatRoom.builder()
        .ownerId(owner.getId()).build();

    chatRoom = chatRoomRepository.save(chatRoom);

    ChatRoomParticipant chatRoomParticipant = ChatRoomParticipant.builder()
        .user(owner)
        .chatRoom(chatRoom)
        .build();

    chatRoomParticipantRepository.save(chatRoomParticipant);

    return ChatRoomDto.fromChatRoomWithHeadcount(chatRoom, 1);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatRoomDto> getAllWithN1() {
    return chatRoomRepository.findAll()
        .stream()
        .map(chatRoom -> ChatRoomDto.fromChatRoomWithHeadcount(
            chatRoom,
            chatRoomParticipantRepository.countByChatRoomId(chatRoom.getId())
        ))
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatRoomDto> getAll() {
    return chatRoomParticipantRepository.getAllChatRoomContentWithHeadcountDto()
        .stream()
        .map(ChatRoomDto::from).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ChatRoomDto getById(UUID id) {
    return null;
  }

  @Override
  @Transactional
  public ChatRoomDto join(UUID chatRoomId, UUID userId) {
    return null;
  }
}
