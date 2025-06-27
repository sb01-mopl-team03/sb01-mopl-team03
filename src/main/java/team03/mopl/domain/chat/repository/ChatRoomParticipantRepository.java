package team03.mopl.domain.chat.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.chat.entity.ChatRoom;
import team03.mopl.domain.chat.entity.ChatRoomParticipant;
import team03.mopl.domain.user.User;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, UUID> {

  Optional<ChatRoomParticipant> findByUserAndChatRoom(User user, ChatRoom chatRoom);

  boolean existsChatRoomParticipantByChatRoomAndUser(ChatRoom chatRoom, User user);

  long countByChatRoomId(UUID chatRoomId);
}
