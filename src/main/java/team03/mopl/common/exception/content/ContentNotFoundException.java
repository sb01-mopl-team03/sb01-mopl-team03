package team03.mopl.common.exception.content;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.domain.content.Content;

public class ContentNotFoundException extends ContentException {

  public ContentNotFoundException() {
    super(ErrorCode.CONTENT_NOT_FOUND);
  }

  public ContentNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ContentNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
