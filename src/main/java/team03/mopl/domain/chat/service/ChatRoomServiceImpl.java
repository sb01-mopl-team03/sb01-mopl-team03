package team03.mopl.domain.chat.service;


import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.domain.chat.dto.ChatRoomDto;
import team03.mopl.domain.chat.repository.ChatRoomRepository;

@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

  private final ChatRoomRepository chatRoomRepository;

  @Override
  public ChatRoomDto create() {
    return null;
  }

  @Override
  public List<ChatRoomDto> getAll() {
    return List.of();
  }

  @Override
  public ChatRoomDto getById(UUID id) {
    return null;
  }
}
