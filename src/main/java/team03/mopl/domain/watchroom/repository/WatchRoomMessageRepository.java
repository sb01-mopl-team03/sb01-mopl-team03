package team03.mopl.domain.watchroom.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.watchroom.entity.WatchRoomMessage;
import team03.mopl.domain.watchroom.entity.WatchRoom;

public interface WatchRoomMessageRepository extends JpaRepository<WatchRoomMessage, UUID> {

  List<WatchRoomMessage> findAllByChatRoom(WatchRoom watchRoom);
}
