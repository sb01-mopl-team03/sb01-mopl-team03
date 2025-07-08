package team03.mopl.domain.watchroom.exception;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class WatchRoomRoomNotFoundException extends WatchRoomException {

  public WatchRoomRoomNotFoundException() {
    super(ErrorCode.CHAT_ROOM_NOT_FOUND);
  }

  public WatchRoomRoomNotFoundException(Throwable cause) {
    super(ErrorCode.CHAT_ROOM_NOT_FOUND, cause);
  }

  public WatchRoomRoomNotFoundException(Map<String, Object> details) {
    super(ErrorCode.CHAT_ROOM_NOT_FOUND, details);
  }

}
