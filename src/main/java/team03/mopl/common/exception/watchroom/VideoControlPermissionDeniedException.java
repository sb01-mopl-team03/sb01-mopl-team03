package team03.mopl.common.exception.watchroom;

import team03.mopl.common.exception.ErrorCode;

public class VideoControlPermissionDeniedException extends WatchRoomException {

  public VideoControlPermissionDeniedException() {
    super(ErrorCode.WATCH_ROOM_PERMISSION_DENIED);
  }
}
