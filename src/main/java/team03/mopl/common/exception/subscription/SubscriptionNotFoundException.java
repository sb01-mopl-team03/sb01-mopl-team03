package team03.mopl.common.exception.subscription;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class SubscriptionNotFoundException extends SubscriptionException {
  public SubscriptionNotFoundException() {
    super(ErrorCode.SUBSCRIPTION_NOT_FOUND);
  }

  public SubscriptionNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public SubscriptionNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
