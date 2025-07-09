package team03.mopl.domain.playlist.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.playlist.entity.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

  List<Playlist> findAllByUserId(UUID userId);
}
