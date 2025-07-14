package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PlaylistContentAlreadyExistsException extends PlaylistException {

  public PlaylistContentAlreadyExistsException() {
    super(ErrorCode.PLAYLIST_CONTENT_ALREADY_EXISTS);
  }

  public PlaylistContentAlreadyExistsException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistContentAlreadyExistsException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
