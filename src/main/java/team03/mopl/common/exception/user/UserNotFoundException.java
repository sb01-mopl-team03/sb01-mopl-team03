package team03.mopl.common.exception.user;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class UserNotFoundException extends UserException {

  public UserNotFoundException() {
    super(ErrorCode.USER_NOT_FOUND);
  }

  public UserNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public UserNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
