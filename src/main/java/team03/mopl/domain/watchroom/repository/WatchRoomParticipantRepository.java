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

  Optional<WatchRoomParticipant> findByUserAndChatRoom(User user, WatchRoom watchRoom);

  boolean existsChatRoomParticipantByChatRoomAndUser(WatchRoom watchRoom, User user);

  //todo - 개선: N+1 문제 발생 가능
  /** deprecated **/
  long countByChatRoomId(UUID chatRoomId);

  List<WatchRoomParticipant> findByChatRoom(WatchRoom watchRoom);

}
