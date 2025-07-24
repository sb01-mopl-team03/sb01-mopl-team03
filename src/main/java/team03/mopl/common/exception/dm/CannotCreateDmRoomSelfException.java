package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class CannotCreateDmRoomSelfException extends DmRoomException{

  public CannotCreateDmRoomSelfException() {
    super(ErrorCode.CANNOT_CREATE_DM_ROOM_SELF);
  }

  public CannotCreateDmRoomSelfException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public CannotCreateDmRoomSelfException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
