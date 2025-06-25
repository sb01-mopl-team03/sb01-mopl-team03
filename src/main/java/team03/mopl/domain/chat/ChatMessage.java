package team03.mopl.domain.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import team03.mopl.domain.user.User;

import jakarta.persistence.Id;
import java.util.UUID;

@Entity
@Getter
@Table(name="chat_messages")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="sender_id")
  private User sender;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name="room_id")
  private ChatRoom chatRoom;

  @Column(name = "content", nullable = false)
  private String content;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}
