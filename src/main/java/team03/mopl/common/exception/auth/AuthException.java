package team03.mopl.common.exception.auth;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class AuthException extends MoplException {

  public AuthException(ErrorCode errorCode) {
    super(errorCode);
  }

  public AuthException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public AuthException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
