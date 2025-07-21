package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class NoOneMatchInDmRoomException extends DmRoomException {

  public NoOneMatchInDmRoomException() {
    super(ErrorCode.NO_ONE_MATCH_IN_DM_ROOM);
  }

  public NoOneMatchInDmRoomException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public NoOneMatchInDmRoomException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
