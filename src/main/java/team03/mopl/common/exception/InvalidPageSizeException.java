package team03.mopl.common.exception;

import java.util.Map;
import team03.mopl.common.exception.curation.CurationException;

public class InvalidPageSizeException extends CurationException {

  public InvalidPageSizeException() {
    super(ErrorCode.INVALID_PAGE_SIZE);
  }

  public InvalidPageSizeException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public InvalidPageSizeException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
