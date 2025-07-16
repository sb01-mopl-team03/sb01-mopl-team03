package team03.mopl.common.exception.auth;

import team03.mopl.common.exception.ErrorCode;

public class TempPasswordExpiredException extends AuthException {

  public TempPasswordExpiredException() {
    super(ErrorCode.TEMP_PASSWORD_EXPIRED);
  }
}
