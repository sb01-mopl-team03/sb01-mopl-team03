package team03.mopl.domain.chat.exception;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class AlreadyJoinedChatRoomException extends ChatException {

  public AlreadyJoinedChatRoomException() {
    super(ErrorCode.ALREADY_JOINED_CHATROOM);
  }

  public AlreadyJoinedChatRoomException(Throwable cause) {
    super(ErrorCode.ALREADY_JOINED_CHATROOM, cause);
  }

  public AlreadyJoinedChatRoomException(Map<String, Object> details) {
    super(ErrorCode.ALREADY_JOINED_CHATROOM, details);
  }
}
