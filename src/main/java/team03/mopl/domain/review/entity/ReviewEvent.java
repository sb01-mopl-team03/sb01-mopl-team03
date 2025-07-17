package team03.mopl.domain.review.entity;

import java.util.UUID;

public record ReviewEvent(
    UUID contentId,
    EventType eventType
) {
  public enum EventType {
    CREATED, UPDATED, DELETED
  }

  public static ReviewEvent created(UUID contentId) {
    return new ReviewEvent(contentId, EventType.CREATED);
  }

  public static ReviewEvent updated(UUID contentId) {
    return new ReviewEvent(contentId, EventType.UPDATED);
  }

  public static ReviewEvent deleted(UUID contentId) {
    return new ReviewEvent(contentId, EventType.DELETED);
  }
}
