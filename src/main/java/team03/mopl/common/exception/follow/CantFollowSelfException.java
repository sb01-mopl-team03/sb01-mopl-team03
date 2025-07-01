package team03.mopl.common.exception.follow;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class CantFollowSelfException extends FollowException {

  public CantFollowSelfException() {
    super(ErrorCode.CANNOT_FOLLOW_SELF);
  }

  public CantFollowSelfException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public CantFollowSelfException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
