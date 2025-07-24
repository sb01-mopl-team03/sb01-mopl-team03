package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class OutUserFromDmRoomException extends DmRoomException {

  public OutUserFromDmRoomException() {
    super(ErrorCode.OUT_USER_FROM_DM_ROOM);
  }


  public OutUserFromDmRoomException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public OutUserFromDmRoomException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
