package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class PlaylistException extends MoplException {
  public PlaylistException(ErrorCode errorCode) {
    super(errorCode);
  }

  public PlaylistException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}