package team03.mopl.common.exception.subscription;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class SelfSubscriptionNotAllowedException extends SubscriptionException {

  public SelfSubscriptionNotAllowedException() {
    super(ErrorCode.REVIEW_NOT_FOUND);
  }

  public SelfSubscriptionNotAllowedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public SelfSubscriptionNotAllowedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
