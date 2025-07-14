package team03.mopl.domain.playlist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.playlist.entity.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

  List<Playlist> findAllByUserId(UUID userId);

  @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.playlistContents pc LEFT JOIN FETCH pc.content WHERE p.id = :playlistId")
  Optional<Playlist> findByIdWithContents(@Param("playlistId") UUID playlistId);
}
