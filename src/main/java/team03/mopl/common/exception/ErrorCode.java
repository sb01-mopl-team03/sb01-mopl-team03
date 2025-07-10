package team03.mopl.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
  DUPLICATED_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용중인 이메일입니다"),
  DUPLICATED_NAME(HttpStatus.CONFLICT, "USER_003", "이미 사용 중인 사용자명입니다."),

  //Content
  CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_001", "존재하지 않는 콘텐츠입니다."),

  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001", "존재하지 않는 리뷰입니다."),
  REVIEW_DELETE_DENIED(HttpStatus.FORBIDDEN, "REVIEW_002", "본인의 리뷰만 삭제할 수 있습니다."),
  REVIEW_UPDATE_DENIED(HttpStatus.FORBIDDEN, "REVIEW_003", "본인의 리뷰만 수정할 수 있습니다."),

  CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM_001", "존재하지 않는 채팅방입니다."),
  ALREADY_JOINED_CHATROOM(HttpStatus.CREATED, "CHATROOM_002", "이미 참여중인 채팅방입니다."),
  //DM
  DM_NOT_FOUND(HttpStatus.NOT_FOUND, "DM_001", "존재하지 않는 다이렉트 메시지입니다."),
  DM_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "DM_001", "존재하지 않는 채팅방입니다."),
  DM_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "DM_002", "메시지 길이가 255자를 초과할 수 없습니다."),
  //Follow
  FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLLOW_001", "존재하지 않는 팔로우입니다."),
  ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "FOLLOW_002", "이미 팔로우하고 있습니다."),
  CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "FOLLOW_003", "자기 자신을 팔로우하고 있습니다."),
  //Notification
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "존재하지 않는 알림입니다."),

  KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "KEYWORD_001", "해당 사용자의 키워드를 찾을 수 없습니다."),
  KEYWORD_DELETE_DENIED_EXCEPTION(HttpStatus.FORBIDDEN, "KEYWORD_002", "본인의 키워드만 삭제할 수 있습니다."),

  SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION_001", "구독 기록을 찾을 수 없습니다."),
  ALREADY_SUBSCRIBED(HttpStatus.CONFLICT, "SUBSCRIPTION_002", "이미 구독 중인 플레이리스트입니다."),
  SELF_SUBSCRIPTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SUBSCRIPTION_003", "자신의 플레이리스트는 구독할 수 없습니다."),
  SUBSCRIPTION_DELETE_DENIED(HttpStatus.FORBIDDEN, "SUBSCRIPTION_004", "구독자만 구독을 취소할 수 있습니다."),

  PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYLIST_001", "존재하지 않는 플레이리스트입니다."),
  PLAYLIST_DELETE_DENIED(HttpStatus.FORBIDDEN, "PLAYLIST_002", "본인의 플레이리스트만 삭제할 수 있습니다."),
  PLAYLIST_UPDATE_DENIED(HttpStatus.FORBIDDEN, "PLAYLIST_003", "본인의 플레이리스트만 수정할 수 있습니다.");

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
