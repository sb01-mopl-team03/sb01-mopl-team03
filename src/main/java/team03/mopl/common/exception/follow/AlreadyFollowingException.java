package team03.mopl.common.exception.follow;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class AlreadyFollowingException extends FollowException {

  public AlreadyFollowingException() {
    super(ErrorCode.ALREADY_FOLLOWING);
  }

  public AlreadyFollowingException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public AlreadyFollowingException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
