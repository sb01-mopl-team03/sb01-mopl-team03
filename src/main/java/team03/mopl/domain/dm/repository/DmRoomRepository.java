package team03.mopl.domain.dm.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.dm.entity.DmRoom;

public interface DmRoomRepository extends JpaRepository<DmRoom, UUID> {

  @Query("SELECT r FROM DmRoom r WHERE (r.senderId = :user1 AND r.receiverId = :user2) OR (r.senderId = :user2 AND r.receiverId = :user1)")
  Optional<DmRoom> findByRoomBetweenUsers(@Param("user1") UUID userA, @Param("user2") UUID userB);

  List<DmRoom> findBySenderIdOrReceiverId(UUID senderId, UUID receiverId);
}
