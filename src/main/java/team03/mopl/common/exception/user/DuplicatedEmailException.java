package team03.mopl.common.exception.user;

import team03.mopl.common.exception.ErrorCode;

public class DuplicatedEmailException extends UserException {

  public DuplicatedEmailException() {
    super(ErrorCode.DUPLICATED_EMAIL);
  }
}
