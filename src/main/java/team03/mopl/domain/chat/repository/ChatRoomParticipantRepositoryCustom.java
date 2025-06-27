package team03.mopl.domain.chat.repository;

import java.util.List;
import team03.mopl.domain.chat.dto.ChatRoomContentWithHeadcountDto;
import team03.mopl.domain.chat.entity.ChatRoom;

public interface ChatRoomParticipantRepositoryCustom {

  List<ChatRoomContentWithHeadcountDto> getAllChatRoomContentWithHeadcountDto();

}
