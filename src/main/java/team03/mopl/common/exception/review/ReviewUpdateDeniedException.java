package team03.mopl.common.exception.review;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.content.ContentException;
import team03.mopl.domain.content.Content;

public class ReviewUpdateDeniedException extends ReviewException {

  public ReviewUpdateDeniedException() {
    super(ErrorCode.REVIEW_UPDATE_DENIED);
  }

  public ReviewUpdateDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ReviewUpdateDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
