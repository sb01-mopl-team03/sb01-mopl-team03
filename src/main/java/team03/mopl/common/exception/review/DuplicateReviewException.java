package team03.mopl.common.exception.review;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class DuplicateReviewException extends ReviewException {

  public DuplicateReviewException() {
    super(ErrorCode.DUPLICATED_REVIEW);
  }

  public DuplicateReviewException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public DuplicateReviewException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}