package team03.mopl.common.exception.curation;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class CurationException extends MoplException {

  public CurationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public CurationException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public CurationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}