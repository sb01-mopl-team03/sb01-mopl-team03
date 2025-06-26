package team03.mopl.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
  DUPLICATED_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용중인 이메일입니다"),
  DUPLICATED_NAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 사용자명입니다."),

  CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM_001", "존재하지 않는 채팅방입니다.");

  private HttpStatus status;
  // 추적하기 쉽도록하는 필드
  private String code;
  private final String message;

  ErrorCode(HttpStatus status , String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
