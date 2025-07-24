package team03.mopl.common.exception.curation;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class KeywordAccessDeniedException extends CurationException {
  public KeywordAccessDeniedException() {
    super(ErrorCode.KEYWORD_ACCESS_DENIED);
  }

  public KeywordAccessDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public KeywordAccessDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
