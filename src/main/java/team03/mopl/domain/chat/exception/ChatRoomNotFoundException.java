package team03.mopl.domain.chat.exception;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class ChatRoomNotFoundException extends ChatException {

  public ChatRoomNotFoundException() {
    super(ErrorCode.CHAT_ROOM_NOT_FOUND);
  }

  public ChatRoomNotFoundException(Throwable cause) {
    super(ErrorCode.CHAT_ROOM_NOT_FOUND, cause);
  }

  public ChatRoomNotFoundException(Map<String, Object> details) {
    super(ErrorCode.CHAT_ROOM_NOT_FOUND, details);
  }

}
