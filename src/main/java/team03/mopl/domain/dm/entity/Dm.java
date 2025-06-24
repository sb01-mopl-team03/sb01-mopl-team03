package team03.mopl.domain.dm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;

@Entity
@Table(name = "dm")
@Getter
public class Dm {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "sender_id", nullable = false)
  private UUID senderId;

  @Column(name = "content", length = 255, nullable = false)
  private String content;

  @Column(name = "is_read", nullable = false)
  private boolean isRead = false;

  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dm_room_id", nullable = false)
  private DmRoom dmRoom;

  public void setDmRoom(DmRoom dmRoom) {
    this.dmRoom = dmRoom;

    if (!dmRoom.getMessages().contains(this)) {
      dmRoom.getMessages().add(this);
    }
  }

  protected Dm() {}

  public Dm(UUID senderId, String content) {
    this.senderId = senderId;
    this.content = content;
  }
}
