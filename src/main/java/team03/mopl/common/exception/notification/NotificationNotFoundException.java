package team03.mopl.common.exception.notification;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.review.ReviewException;

public class NotificationNotFoundException extends ReviewException {

  public NotificationNotFoundException() {
    super(ErrorCode.NOTIFICATION_NOT_FOUND);
  }

  public NotificationNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public NotificationNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
