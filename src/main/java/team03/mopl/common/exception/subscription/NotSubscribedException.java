package team03.mopl.common.exception.subscription;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class NotSubscribedException extends SubscriptionException {
  public NotSubscribedException() {
    super(ErrorCode.NOT_SUBSCRIBED);
  }

  public NotSubscribedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public NotSubscribedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
