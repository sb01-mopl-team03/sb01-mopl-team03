package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PlaylistDeniedException extends PlaylistException {

  public PlaylistDeniedException() {
    super(ErrorCode.PLAYLIST_DENIED);
  }

  public PlaylistDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}