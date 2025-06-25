package team03.mopl.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import team03.mopl.domain.content.Content;


@Entity
@Table(name = "rooms")
@Getter
@NoArgsConstructor
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  public ChatRoom(Content content) {
    this.content = content;
  }
}
