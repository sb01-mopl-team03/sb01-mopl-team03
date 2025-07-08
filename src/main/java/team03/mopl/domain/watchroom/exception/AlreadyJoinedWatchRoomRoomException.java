package team03.mopl.domain.watchroom.exception;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class AlreadyJoinedWatchRoomRoomException extends WatchRoomException {

  public AlreadyJoinedWatchRoomRoomException() {
    super(ErrorCode.ALREADY_JOINED_CHATROOM);
  }

  public AlreadyJoinedWatchRoomRoomException(Throwable cause) {
    super(ErrorCode.ALREADY_JOINED_CHATROOM, cause);
  }

  public AlreadyJoinedWatchRoomRoomException(Map<String, Object> details) {
    super(ErrorCode.ALREADY_JOINED_CHATROOM, details);
  }
}
