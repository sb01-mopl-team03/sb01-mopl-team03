package team03.mopl.common.exception.follow;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class FollowException extends MoplException {
  public FollowException(ErrorCode errorCode) {
    super(errorCode);
  }

  public FollowException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public FollowException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}