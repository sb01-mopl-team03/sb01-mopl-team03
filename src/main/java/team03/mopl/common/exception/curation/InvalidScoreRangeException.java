package team03.mopl.common.exception.curation;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class InvalidScoreRangeException extends CurationException {

  public InvalidScoreRangeException() {
    super(ErrorCode.INVALID_SCORE_RANGE);
  }

  public InvalidScoreRangeException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public InvalidScoreRangeException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
