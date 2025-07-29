package team03.mopl.common.exception.watchroom;

import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class UnsupportedVideoControlActionException extends WatchRoomException {

  public UnsupportedVideoControlActionException() {
    super(ErrorCode.WATCH_ROOM_INVALID_CONTROL_ACTION);
  }

}
