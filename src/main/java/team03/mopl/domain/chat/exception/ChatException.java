package team03.mopl.domain.chat.exception;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class ChatException extends MoplException {

  public ChatException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ChatException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public ChatException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
