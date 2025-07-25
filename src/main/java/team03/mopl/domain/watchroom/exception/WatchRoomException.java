package team03.mopl.domain.watchroom.exception;

import team03.mopl.common.exception.ErrorCode;
import team03.mopl.common.exception.MoplException;

public class WatchRoomException extends MoplException {

  public WatchRoomException(ErrorCode errorCode) {
    super(errorCode);
  }

}
