package team03.mopl.common.exception.review;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class ReviewException extends MoplException {
  public ReviewException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ReviewException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ReviewException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}