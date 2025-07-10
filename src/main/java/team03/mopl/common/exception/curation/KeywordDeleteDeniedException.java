package team03.mopl.common.exception.curation;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class KeywordDeleteDeniedException extends CurationException {
  public KeywordDeleteDeniedException() {
    super(ErrorCode.KEYWORD_DELETE_DENIED_EXCEPTION);
  }

  public KeywordDeleteDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public KeywordDeleteDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
