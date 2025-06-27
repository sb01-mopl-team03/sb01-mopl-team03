package team03.mopl.common.exception.content;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class ContentException extends MoplException {
  public ContentException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ContentException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ContentException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}