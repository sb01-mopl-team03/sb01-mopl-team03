package team03.mopl.common.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class MoplException extends RuntimeException {
  private final Map<String, Object> details = new HashMap<>();
  private final ErrorCode errorCode;

  public MoplException(ErrorCode errorCode) {
    super(errorCode.getMessage(), null, false, false);
    this.errorCode = errorCode;
  }

  public MoplException(ErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
  }

  public MoplException(ErrorCode errorCode, Map<String, Object> details){
    this.errorCode = errorCode;
    this.details.putAll(details);
  }

}
