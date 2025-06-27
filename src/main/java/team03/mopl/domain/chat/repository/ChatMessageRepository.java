package team03.mopl.domain.chat.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.chat.entity.ChatMessage;
import team03.mopl.domain.chat.entity.ChatRoom;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  List<ChatMessage> findAllByChatRoom(ChatRoom chatRoom);
}
