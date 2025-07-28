package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PlaylistContentEmptyException extends PlaylistException {

  public PlaylistContentEmptyException() {
    super(ErrorCode.PLAYLIST_CONTENT_EMPTY);
  }

  public PlaylistContentEmptyException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistContentEmptyException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}