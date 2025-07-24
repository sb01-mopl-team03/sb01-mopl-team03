package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class AlreadyDmRoomExistException extends DmRoomException{

  public AlreadyDmRoomExistException() {
    super(ErrorCode.ALREADY_DM_ROOM_EXIST);
  }


  public AlreadyDmRoomExistException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public AlreadyDmRoomExistException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
