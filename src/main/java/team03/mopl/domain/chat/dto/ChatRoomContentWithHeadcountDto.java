package team03.mopl.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import team03.mopl.domain.chat.entity.ChatRoom;
import team03.mopl.domain.content.Content;

@AllArgsConstructor
@Getter
@Setter
public class ChatRoomContentWithHeadcountDto {

  private ChatRoom chatRoom;
  private Content content;
  Long headCount;

}
