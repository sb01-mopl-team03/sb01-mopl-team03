package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PlaylistUpdateDeniedException extends PlaylistException {

  public PlaylistUpdateDeniedException() {
    super(ErrorCode.PLAYLIST_UPDATE_DENIED);
  }

  public PlaylistUpdateDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistUpdateDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}