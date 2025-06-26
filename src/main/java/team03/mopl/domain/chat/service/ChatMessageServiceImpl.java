package team03.mopl.domain.chat.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.chat.dto.ChatMessageCreateRequest;
import team03.mopl.domain.chat.dto.ChatMessageDto;
import team03.mopl.domain.chat.entity.ChatMessage;
import team03.mopl.domain.chat.entity.ChatRoom;
import team03.mopl.domain.chat.exception.ChatRoomNotFoundException;
import team03.mopl.domain.chat.repository.ChatMessageRepository;
import team03.mopl.domain.chat.repository.ChatRoomParticipantRepository;
import team03.mopl.domain.chat.repository.ChatRoomRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomParticipantRepository chatRoomParticipantRepository;
  private final UserRepository userRepository;

  @Override
  public ChatMessageDto create(ChatMessageCreateRequest request) {

    User sender = userRepository.findById(request.userId())
        .orElseThrow(UserNotFoundException::new);
    ChatRoom chatRoom = chatRoomRepository.findById(request.chatRoomId())
        .orElseThrow(ChatRoomNotFoundException::new);

    if (!chatRoomParticipantRepository.existsChatRoomParticipantByChatRoomAndUser(chatRoom, sender))
    {
      throw new ChatRoomNotFoundException();
    }

    ChatMessage chatMessage = ChatMessage.builder()
        .sender(sender)
        .chatRoom(chatRoom)
        .content(request.content())
        .build();

    return ChatMessageDto.from(chatMessageRepository.save(chatMessage));
  }

  @Override
  public List<ChatMessageDto> getAllByRoomId(UUID chatRoomId, UUID userId) {

    //todo - userId를 이후에 context에서 가져올 수 있게되면 userId 파라미터 제거하여 리팩토링

    User sender = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);
    ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
        .orElseThrow(ChatRoomNotFoundException::new);

    if (!chatRoomParticipantRepository.existsChatRoomParticipantByChatRoomAndUser(chatRoom, sender))
    {
      throw new ChatRoomNotFoundException();
    }

    List<ChatMessage> chatMessages = chatMessageRepository.findAllByChatRoom(chatRoom);

    return chatMessages.stream().map(ChatMessageDto::from).toList();
  }
}
