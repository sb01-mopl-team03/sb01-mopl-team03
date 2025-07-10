package team03.mopl.common.exception.subscription;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class AlreadySubscribedException extends SubscriptionException{
  public AlreadySubscribedException() {
    super(ErrorCode.REVIEW_NOT_FOUND);
  }

  public AlreadySubscribedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public AlreadySubscribedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }

}
