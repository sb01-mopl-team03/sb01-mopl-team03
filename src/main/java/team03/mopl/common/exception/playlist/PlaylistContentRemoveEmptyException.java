package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PlaylistContentRemoveEmptyException extends PlaylistException {

  public PlaylistContentRemoveEmptyException() {
    super(ErrorCode.PLAYLIST_CONTENT_REMOVE_EMPTY);
  }

  public PlaylistContentRemoveEmptyException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistContentRemoveEmptyException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
