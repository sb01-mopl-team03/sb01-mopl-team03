package team03.mopl.common.exception.playlist;

import java.util.Map;
import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.content.ContentException;
import team03.mopl.common.exception.review.ReviewException;
import team03.mopl.domain.content.Content;

public class PlaylistNotFoundException extends PlaylistException {

  public PlaylistNotFoundException() {
    super(ErrorCode.PLAYLIST_NOT_FOUND);
  }

  public PlaylistNotFoundException(ErrorCode errorCode, Throwable cause) {
    super(errorCode, cause);
  }

  public PlaylistNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);

  }
}
