package team03.mopl.common.exception.auth;

import team03.mopl.common.exception.ErrorCode;

public class InvalidPasswordException extends AuthException {

  public InvalidPasswordException() {
    super(ErrorCode.INVALID_PASSWORD);
  }
}
