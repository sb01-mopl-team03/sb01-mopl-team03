package team03.mopl.domain.chat.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.domain.chat.dto.ChatMessageCreateRequest;
import team03.mopl.domain.chat.dto.ChatMessageDto;
import team03.mopl.domain.chat.entity.ChatMessage;
import team03.mopl.domain.chat.repository.ChatMessageRepository;
import team03.mopl.domain.chat.repository.ChatRoomParticipantRepository;
import team03.mopl.domain.chat.repository.ChatRoomRepository;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

  private final ChatMessageRepository chatMessageRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomParticipantRepository chatRoomParticipantRepository;
  //private final UserRepository userRepository;

  @Override
  public ChatMessageDto create(ChatMessageCreateRequest request) {
    return null;
  }

  @Override
  public List<ChatMessageDto> getAllByRoomId(UUID chatRoomId) {
    return null;
  }
}
