package team03.mopl.domain.watchroom.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.watchroom.entity.WatchRoom;
import team03.mopl.domain.watchroom.entity.WatchRoomParticipant;
import team03.mopl.domain.user.User;

public interface WatchRoomParticipantRepository extends JpaRepository<WatchRoomParticipant, UUID>,
    WatchRoomParticipantRepositoryCustom {

  Optional<WatchRoomParticipant> findByUserAndWatchRoom(User user, WatchRoom watchRoom);

  boolean existsWatchRoomParticipantByWatchRoomAndUser(WatchRoom watchRoom, User user);

  Optional<WatchRoomParticipant> findFirstByWatchRoom(WatchRoom watchRoom);

  List<WatchRoomParticipant> findByWatchRoom(WatchRoom watchRoom);
}
