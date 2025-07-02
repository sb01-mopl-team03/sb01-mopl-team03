package team03.mopl.domain.chat.dto;

import java.util.UUID;
import team03.mopl.domain.chat.entity.ChatRoom;


public record ChatRoomDto(

    UUID id,
    UUID contentId,
    //ContentDto contentDto,
    UUID ownerId,
    //List<UserDto> userList,
    Long headCount

) {

  public static ChatRoomDto fromChatRoomWithHeadcount(ChatRoom chatRoom, long headcount ) {
    return new ChatRoomDto(
        chatRoom.getId(),
        chatRoom.getContent().getId(),
        chatRoom.getOwnerId(),
        headcount
    );
  }

  public static ChatRoomDto from(ChatRoomContentWithHeadcountDto chatRoomContentWithHeadcountDto) {
    return new ChatRoomDto(
        chatRoomContentWithHeadcountDto.getChatRoom().getId(),
        //chatRoomContentWithHeadcountDto.getContent(),
        chatRoomContentWithHeadcountDto.getContent().getId(),
        chatRoomContentWithHeadcountDto.getChatRoom().getOwnerId(),
        chatRoomContentWithHeadcountDto.getHeadCount()
    );
  }

}
