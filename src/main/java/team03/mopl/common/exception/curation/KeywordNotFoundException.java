package team03.mopl.common.exception.curation;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class KeywordNotFoundException extends CurationException {
  public KeywordNotFoundException() {
    super(ErrorCode.KEYWORD_NOT_FOUND);
  }

  public KeywordNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public KeywordNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
