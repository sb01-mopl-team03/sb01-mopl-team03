package team03.mopl.domain.dm.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.dm.entity.Dm;

public interface DmRepository extends JpaRepository<Dm, UUID> {
  @Query("SELECT d FROM Dm d WHERE d.dmRoom.id = :roomId ORDER BY d.createdAt ASC")
  List<Dm> findByDmRoomIdOrderByCreatedAtAsc(@Param("roomId") UUID roomId);

  long countByDmRoomId(UUID roomId);
}
