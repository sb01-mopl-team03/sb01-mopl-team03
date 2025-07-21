package team03.mopl.common.exception.dm;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class DmDecodingError extends DmRoomException{

  public DmDecodingError() {
    super(ErrorCode.DM_DECODING_ERROR);
  }

  public DmDecodingError(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public DmDecodingError(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
