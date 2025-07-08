package team03.mopl.domain.watchroom.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.watchroom.entity.WatchRoom;

public interface WatchRoomRepository extends JpaRepository<WatchRoom, UUID> {

}
