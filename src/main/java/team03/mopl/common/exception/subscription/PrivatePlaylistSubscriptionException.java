package team03.mopl.common.exception.subscription;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PrivatePlaylistSubscriptionException extends SubscriptionException {

  public PrivatePlaylistSubscriptionException() {
    super(ErrorCode.REVIEW_NOT_FOUND);
  }

  public PrivatePlaylistSubscriptionException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PrivatePlaylistSubscriptionException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
