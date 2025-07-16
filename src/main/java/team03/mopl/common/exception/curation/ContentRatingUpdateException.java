package team03.mopl.common.exception.curation;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class ContentRatingUpdateException extends CurationException {

  public ContentRatingUpdateException() {
    super(ErrorCode.CONTENT_RATING_UPDATE_FAILED);
  }

  public ContentRatingUpdateException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ContentRatingUpdateException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public ContentRatingUpdateException(Throwable cause) {
    super(ErrorCode.CONTENT_RATING_UPDATE_FAILED, cause);
  }
}
