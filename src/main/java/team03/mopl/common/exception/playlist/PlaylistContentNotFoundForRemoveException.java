package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PlaylistContentNotFoundForRemoveException extends PlaylistException {

  public PlaylistContentNotFoundForRemoveException() {
    super(ErrorCode.PLAYLIST_CONTENT_NOT_FOUND_FOR_REMOVE);
  }

  public PlaylistContentNotFoundForRemoveException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistContentNotFoundForRemoveException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
