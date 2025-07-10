package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;

public class PlaylistDeleteDeniedException extends PlaylistException {

  public PlaylistDeleteDeniedException() {
    super(ErrorCode.PLAYLIST_DELETE_DENIED);
  }

  public PlaylistDeleteDeniedException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistDeleteDeniedException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}