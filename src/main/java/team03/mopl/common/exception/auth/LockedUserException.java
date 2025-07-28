package team03.mopl.common.exception.auth;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class LockedUserException extends AuthException {

  public LockedUserException() {
    super(ErrorCode.LOCKED_USER);
  }
}
