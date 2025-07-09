package team03.mopl.common.exception.subscription;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class SubscriptionException extends MoplException {
  public SubscriptionException(ErrorCode errorCode) {
    super(errorCode);
  }

  public SubscriptionException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public SubscriptionException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}