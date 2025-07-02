package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class DmRoomException extends MoplException {

  public DmRoomException(ErrorCode errorCode) {
    super(errorCode);
  }

  public DmRoomException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public DmRoomException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
