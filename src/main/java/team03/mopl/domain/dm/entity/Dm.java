package team03.mopl.domain.dm.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dms")
@Getter
public class Dm {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "sender_id", nullable = false)
  private UUID senderId;

  @Column(name = "content", length = 255, nullable = false)
  private String content;

  /* @Column(name = "is_read", nullable = false)
  private List<UUID> isRead = false;*/
  @ElementCollection
  @CollectionTable(name = "dm_read_users", joinColumns = @JoinColumn(name = "dm_id"))
  @Column(name = "user_id", unique = true)
  private Set<UUID> readUserIds = new HashSet<>();

  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  /*if (!dmRoom.getMessages().contains(this)) {
      dmRoom.getMessages().add(this);
    }*/
  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dm_room_id", nullable = false)
  private DmRoom dmRoom;

  // 메시지를 읽은 사람은 리스트에 추가
  public void readDm(UUID userId) {
    readUserIds.add(userId);
  }
  // 2명 중 읽은 사람 수 만큼 빼기
  public int getUnreadCount() {
    return 2 - readUserIds.size();
  }
  //public void setRead(){    isRead = true;  }

  protected Dm() {}

  public Dm(UUID senderId, String content) {
    this.senderId = senderId;
    this.content = content;
  }
  //테스트를 위한 생성자
  public Dm(UUID id, UUID senderId, String content) {
    this.id = id;
    this.senderId = senderId;
    this.content = content;
  }
}
