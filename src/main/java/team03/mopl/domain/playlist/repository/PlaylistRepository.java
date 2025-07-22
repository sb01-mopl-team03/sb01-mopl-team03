package team03.mopl.domain.playlist.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.playlist.entity.Playlist;

public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

  @Query("SELECT p FROM Playlist p " +
      "WHERE ((p.isPublic = true) OR (p.user.id = :currentUserId)) " +
      "AND p.nameNormalized LIKE CONCAT('%', :normalizedKeyword, '%') " +
      "ORDER BY p.createdAt DESC")
  List<Playlist> searchPlaylistsWithNormalizedKeyword(
      @Param("normalizedKeyword") String normalizedKeyword,
      @Param("currentUserId") UUID currentUserId
  );

  @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
  List<Playlist> findAllByUserId(@Param("userId") UUID userId);

  @Query("SELECT p FROM Playlist p WHERE p.user.id = :userId AND p.isPublic = true ORDER BY p.createdAt DESC")
  List<Playlist> findPublicPlaylistsByUserId(@Param("userId") UUID userId);

  @Query("SELECT p FROM Playlist p LEFT JOIN FETCH p.playlistContents pc LEFT JOIN FETCH pc.content WHERE p.id = :playlistId")
  Optional<Playlist> findByIdWithContents(@Param("playlistId") UUID playlistId);
}
