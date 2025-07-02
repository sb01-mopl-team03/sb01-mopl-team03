package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class DmRoomNotFoundException extends DmRoomException {
  public DmRoomNotFoundException() {
    super(ErrorCode.DM_ROOM_NOT_FOUND);
  }

  public DmRoomNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public DmRoomNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
