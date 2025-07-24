package team03.mopl.domain.subscription;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import team03.mopl.domain.playlist.entity.Playlist;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

  boolean existsByUserIdAndPlaylistId(UUID userId, UUID playlistId);

  List<Subscription> findByPlaylistId(UUID playlistId);

  List<Subscription> findByUserId(UUID userId);

  @Query("SELECT s.playlist FROM Subscription s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
  List<Playlist> findPlaylistsByUserId(UUID userId);

  void deleteByUserIdAndPlaylistId(UUID userId, UUID playlistId);
}
