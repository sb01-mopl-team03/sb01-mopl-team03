package team03.mopl.domain.subscription.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.subscription.dto.SubscriptionDto;

public interface SubscriptionService {

  SubscriptionDto subscribe(UUID userId, UUID playlistId);

  void unsubscribe(UUID subscriptionId, UUID userId);

  List<SubscriptionDto> getSubscriptions(UUID userId); // 사용자의 구독 목록 조회

  List<SubscriptionDto> getSubscribers(UUID playlistId); // 플레이리스트의 구독자 목록 조회

}
