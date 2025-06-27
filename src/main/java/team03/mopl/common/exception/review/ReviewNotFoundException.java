package team03.mopl.common.exception.review;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.content.ContentException;
import team03.mopl.domain.content.Content;

public class ReviewNotFoundException extends ReviewException {

  public ReviewNotFoundException() {
    super(ErrorCode.REVIEW_NOT_FOUND);
  }

  public ReviewNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ReviewNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
