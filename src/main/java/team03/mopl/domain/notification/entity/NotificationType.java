package team03.mopl.domain.notification.entity;

import lombok.Getter;

@Getter
public enum NotificationType {
  ROLE_CHANGED("role_changed"),
  PLAYLIST_SUBSCRIBED("play_subscribed"),
  FOLLOWING_POSTED_PLAYLIST("following_posted_playlist"),
  FOLLOWED("followed"),
  UNFOLLOWED("unfollowed"),
  DM_RECEIVED("dm_received"),
  NEW_DM_ROOM("created new DM room");

  private final String eventName;

  NotificationType(String eventName) {
    this.eventName = eventName;
  }

}
