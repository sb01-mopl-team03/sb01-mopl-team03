package team03.mopl.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST, "PAGINATION_001", "올바르지 않은 커서 형식입니다."),
  INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "PAGINATION_002", "페이지 사이즈가 올바르지 않습니다."),

  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),
  DUPLICATED_EMAIL(HttpStatus.CONFLICT, "USER_002", "이미 사용중인 이메일입니다"),

  INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_001", "비밀번호가 일치하지 않습니다."),
  LOCKED_USER(HttpStatus.FORBIDDEN, "AUTH_002", "잠긴 계정입니다."),
  TEMP_PASSWORD_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_003", "임시 비밀번호가 만료되었습니다."),

  //Content
  CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_001", "존재하지 않는 콘텐츠입니다."),
  CONTENT_RATING_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CONTENT_002", "콘텐츠 평점 업데이트에 실패했습니다."),

  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REVIEW_001", "존재하지 않는 리뷰입니다."),
  REVIEW_DELETE_DENIED(HttpStatus.FORBIDDEN, "REVIEW_002", "본인의 리뷰만 삭제할 수 있습니다."),
  REVIEW_UPDATE_DENIED(HttpStatus.FORBIDDEN, "REVIEW_003", "본인의 리뷰만 수정할 수 있습니다."),
  DUPLICATED_REVIEW(HttpStatus.CONFLICT, "REVIEW_004", "해당 콘텐츠에 이미 리뷰를 작성했습니다."),

  WATCH_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "WATCHROOM_001", "존재하지 않는 시청방입니다."),
  WATCH_ROOM_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "WATCHROOM_002", "방장만 동영상을 제어할 수 있습니다."),
  WATCH_ROOM_INVALID_CONTROL_ACTION(HttpStatus.BAD_REQUEST, "WATCHROOM_003", "지원하지 않는 동영상 제어 액션입니다."),

  //DM
  DM_NOT_FOUND(HttpStatus.NOT_FOUND, "DM_001", "존재하지 않는 다이렉트 메시지입니다."),
  DM_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "DM_001", "존재하지 않는 채팅방입니다."),
  DM_CONTENT_TOO_LONG(HttpStatus.BAD_REQUEST, "DM_002", "메시지 길이가 255자를 초과할 수 없습니다."),
  NO_ONE_MATCH_IN_DM_ROOM(HttpStatus.BAD_REQUEST, "DM_003", "채팅 방의 소속된 유저가 아닙니다."),
  DM_DECODING_ERROR(HttpStatus.BAD_REQUEST, "DM_004", "DM 디코딩 중 오류가 발생했습니다."),
  OUT_USER_FROM_DM_ROOM(HttpStatus.BAD_REQUEST, "DM_005", "두 유저의 채팅방이 존재하며 한 유저가 남아있습니다."),
  ALREADY_DM_ROOM_EXIST(HttpStatus.BAD_REQUEST, "DM_006", "두 유저의 채팅방이 이미 존재합니다."),
  CANNOT_CREATE_DM_ROOM_SELF(HttpStatus.BAD_REQUEST, "DM_007", "자기 자신과 채팅방을 만들 수 없습니다."),
  //Follow
  FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND, "FOLLOW_001", "존재하지 않는 팔로우입니다."),
  ALREADY_FOLLOWING(HttpStatus.BAD_REQUEST, "FOLLOW_002", "이미 팔로우하고 있습니다."),
  CANNOT_FOLLOW_SELF(HttpStatus.BAD_REQUEST, "FOLLOW_003", "자기 자신을 팔로우하고 있습니다."),
  BAD_REQUEST_FOLLOWING(HttpStatus.BAD_REQUEST, "FOLLOW_004", "로그인된 사람과 팔로우하는 사람이 다릅니다"),

  //Notification,
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "존재하지 않는 알림입니다."),

  KEYWORD_NOT_FOUND(HttpStatus.NOT_FOUND, "KEYWORD_001", "해당 사용자의 키워드를 찾을 수 없습니다."),
  KEYWORD_DELETE_DENIED(HttpStatus.FORBIDDEN, "KEYWORD_002", "본인의 키워드만 삭제할 수 있습니다."),
  KEYWORD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "KEYWORD_003", "요청한 키워드에 접근할 수 없습니다."),

  INVALID_SCORE_RANGE(HttpStatus.BAD_REQUEST, "CURATION_001", "점수 범위가 올바르지 않습니다."),

  SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "SUBSCRIPTION_001", "구독 기록을 찾을 수 없습니다."),
  ALREADY_SUBSCRIBED(HttpStatus.CONFLICT, "SUBSCRIPTION_002", "이미 구독 중인 플레이리스트입니다."),
  SELF_SUBSCRIPTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SUBSCRIPTION_003", "자신의 플레이리스트는 구독할 수 없습니다."),
  SUBSCRIPTION_DELETE_DENIED(HttpStatus.FORBIDDEN, "SUBSCRIPTION_004", "구독자만 구독을 취소할 수 있습니다."),
  PRIVATE_PLAYLIST_SUBSCRIPTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "SUBSCRIPTION_005", "비공개 플레이리스트는 구독할 수 없습니다."),

  PLAYLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYLIST_001", "존재하지 않는 플레이리스트입니다."),
  PLAYLIST_DENIED(HttpStatus.FORBIDDEN, "PLAYLIST_002", "플레이리스트에 권한이 없습니다."),
  PLAYLIST_CONTENT_EMPTY(HttpStatus.NO_CONTENT, "PLAYLIST_003", "추가할 컨텐츠 ID 목록이 비어있습니다"),
  PLAYLIST_CONTENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PLAYLIST_004", "모든 컨텐츠가 이미 플레이리스트에 존재합니다"),
  PLAYLIST_CONTENT_REMOVE_EMPTY(HttpStatus.BAD_REQUEST, "PLAYLIST_005", "제거할 컨텐츠 ID 목록이 비어있습니다"),
  PLAYLIST_CONTENT_NOT_FOUND_FOR_REMOVE(HttpStatus.NOT_FOUND, "PLAYLIST_006", "플레이리스트에서 제거할 컨텐츠를 찾을 수 없습니다");

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
