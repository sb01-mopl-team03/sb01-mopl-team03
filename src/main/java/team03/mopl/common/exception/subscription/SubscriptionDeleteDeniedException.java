package team03.mopl.common.exception.subscription;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class SubscriptionDeleteDeniedException extends SubscriptionException {

  public SubscriptionDeleteDeniedException() {
    super(ErrorCode.SUBSCRIPTION_DELETE_DENIED);
  }

  public SubscriptionDeleteDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public SubscriptionDeleteDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
