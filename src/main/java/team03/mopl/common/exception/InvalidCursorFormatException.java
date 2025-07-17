package team03.mopl.common.exception;

import java.util.Map;

public class InvalidCursorFormatException extends MoplException {

  public InvalidCursorFormatException() {
    super(ErrorCode.INVALID_CURSOR_FORMAT);
  }

  public InvalidCursorFormatException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public InvalidCursorFormatException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public InvalidCursorFormatException(Throwable cause) {
    super(ErrorCode.INVALID_CURSOR_FORMAT, cause);
  }
}
