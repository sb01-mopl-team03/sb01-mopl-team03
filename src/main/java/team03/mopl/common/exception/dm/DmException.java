package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class DmException extends MoplException {

  public DmException(ErrorCode errorCode) {
    super(errorCode);
  }

  public DmException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public DmException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
