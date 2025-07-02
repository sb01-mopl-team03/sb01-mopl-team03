package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class DmNotFoundException extends DmException {
  public DmNotFoundException() {
    super(ErrorCode.DM_NOT_FOUND);
  }

  public DmNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public DmNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
