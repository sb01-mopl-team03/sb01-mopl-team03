package team03.mopl.common.exception.notification;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class NotificationException extends MoplException {
  public NotificationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public NotificationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public NotificationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}