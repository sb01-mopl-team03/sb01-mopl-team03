package team03.mopl.common.exception.user;

import team03.mopl.common.exception.ErrorCode;

public class DuplicatedNameException extends UserException {

  public DuplicatedNameException() {
    super(ErrorCode.DUPLICATED_NAME);
  }
}
