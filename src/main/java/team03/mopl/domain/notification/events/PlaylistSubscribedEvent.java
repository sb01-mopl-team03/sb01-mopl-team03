package team03.mopl.domain.notification.events;

import java.util.UUID;

public record PlaylistSubscribedEvent(
    UUID playlistId,
    UUID ownerId,        // 플레이리스트 주인
    String title,
    UUID subscriberId    // 구독한 사용자
) {}

