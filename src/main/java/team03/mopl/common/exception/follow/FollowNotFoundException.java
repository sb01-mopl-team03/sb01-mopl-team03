package team03.mopl.common.exception.follow;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class FollowNotFoundException extends FollowException {

  public FollowNotFoundException() {
    super(ErrorCode.FOLLOW_NOT_FOUND);
  }

  public FollowNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public FollowNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
