package team03.mopl.domain.chat.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.chat.dto.ChatRoomCreateRequest;
import team03.mopl.domain.chat.dto.ChatRoomDto;

public interface ChatRoomService {

  //채팅방 생성
  ChatRoomDto create(ChatRoomCreateRequest request);

  //채팅방 전체 조회
  List<ChatRoomDto> getAll();

  List<ChatRoomDto> getAllWithN1();

  //채팅방 단일 조회
  ChatRoomDto getById(UUID id);

  //채팅방 참여
  ChatRoomDto join(UUID chatRoomId, UUID userId);
}
