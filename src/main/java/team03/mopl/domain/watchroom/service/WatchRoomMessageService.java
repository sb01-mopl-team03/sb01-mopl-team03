package team03.mopl.domain.watchroom.service;


import java.util.List;
import java.util.UUID;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageDto;

public interface WatchRoomMessageService {

  //메세지 생성
  WatchRoomMessageDto create(WatchRoomMessageCreateRequest request, String userEmail);

  //메세지 조회
  List<WatchRoomMessageDto> getAllByRoomId(UUID chatRoomId, UUID userId);
}
