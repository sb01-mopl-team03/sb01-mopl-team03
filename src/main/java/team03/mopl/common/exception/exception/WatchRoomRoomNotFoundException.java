package team03.mopl.common.exception.exception;

import team03.mopl.common.exception.ErrorCode;

public class WatchRoomRoomNotFoundException extends WatchRoomException {

  public WatchRoomRoomNotFoundException() {
    super(ErrorCode.CHAT_ROOM_NOT_FOUND);
  }

}
