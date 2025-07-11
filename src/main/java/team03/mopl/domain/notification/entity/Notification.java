package team03.mopl.domain.notification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "receiver_id", nullable = false)
  private UUID receiverId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 255)
  private NotificationType type;

  @Column(name = "content", nullable = false, length = 255)
  private String content;

  @Column(name = "is_read", nullable = false)
  private boolean isRead = false;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Notification(UUID receiverId, NotificationType type, String content) {
    this.receiverId = receiverId;
    this.type = type;
    this.content = content;
  }
  public void setIsRead() {
    this.isRead = true;
  }
}
