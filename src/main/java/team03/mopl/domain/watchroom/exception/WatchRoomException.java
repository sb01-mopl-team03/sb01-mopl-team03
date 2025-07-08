package team03.mopl.domain.watchroom.exception;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class WatchRoomException extends MoplException {

  public WatchRoomException(ErrorCode errorCode) {
    super(errorCode);
  }

  public WatchRoomException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public WatchRoomException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
