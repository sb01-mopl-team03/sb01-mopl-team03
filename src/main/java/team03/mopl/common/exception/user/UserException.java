package team03.mopl.common.exception.user;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class UserException extends MoplException {

  public UserException(ErrorCode errorCode) {
    super(errorCode);
  }

  public UserException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public UserException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
