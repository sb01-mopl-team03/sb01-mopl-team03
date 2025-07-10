package team03.mopl.common.exception.review;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.content.ContentException;
import team03.mopl.domain.content.Content;

public class ReviewDeleteDeniedException extends ReviewException {

  public ReviewDeleteDeniedException() {
    super(ErrorCode.REVIEW_DELETE_DENIED);
  }

  public ReviewDeleteDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ReviewDeleteDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
