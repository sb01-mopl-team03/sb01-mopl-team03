package team03.mopl.common.exception.follow;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class BadRequestFollowingException extends FollowException{

  public BadRequestFollowingException() {
    super(ErrorCode.BAD_REQUEST_FOLLOWING);
  }

  public BadRequestFollowingException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public BadRequestFollowingException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
