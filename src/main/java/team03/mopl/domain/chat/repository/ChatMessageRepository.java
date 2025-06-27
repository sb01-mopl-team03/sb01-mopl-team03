package team03.mopl.domain.chat.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

}
