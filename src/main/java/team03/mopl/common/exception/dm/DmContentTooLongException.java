package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class DmContentTooLongException extends DmException {

  public DmContentTooLongException() {
    super(ErrorCode.DM_CONTENT_TOO_LONG);
  }

  public DmContentTooLongException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public DmContentTooLongException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
