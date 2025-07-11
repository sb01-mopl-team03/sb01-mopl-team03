package team03.mopl.domain.subscription;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  boolean existsByUserIdAndPlaylistId(UUID userId, UUID playlistId);

  List<Subscription> findByPlaylistId(UUID playlistId);

  List<Subscription> findByUserId(UUID userId);

  void deleteByUserIdAndPlaylistId(UUID userId, UUID playlistId);

}
