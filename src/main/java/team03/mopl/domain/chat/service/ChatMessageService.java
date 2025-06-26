package team03.mopl.domain.chat.service;


import java.util.List;
import java.util.UUID;
import team03.mopl.domain.chat.dto.ChatMessageCreateRequest;
import team03.mopl.domain.chat.dto.ChatMessageDto;

public interface ChatMessageService {

  //메세지 생성
  ChatMessageDto create(ChatMessageCreateRequest request);

  //메세지 조회
  List<ChatMessageDto> getAllByRoomId(UUID chatRoomId);
}
