package team03.mopl.domain.notification.entity;

public enum NotificationType {
  ROLE_CHANGED,              // 권한 변경
  PLAYLIST_SUBSCRIBED,       // 내 재생목록을 구독
  FOLLOWING_POSTED_PLAYLIST, // 팔로우한 사용자가 재생목록 등록
  FOLLOWED,                  // 다른 사용자가 나를 팔로우
  DM_RECEIVED;               // DM 수신
}
