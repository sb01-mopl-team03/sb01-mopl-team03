package team03.mopl.domain.dm.entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(name = "dm_rooms")
@EntityListeners(AuditingEntityListener.class)
public class DmRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Setter
  @Column(name = "sender_id")
  private UUID senderId;

  @Setter
  @Column(name = "receiver_id")
  private UUID receiverId;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "last_message_at")
  private LocalDateTime lastMessageAt;

  @ElementCollection
  @CollectionTable(
      name = "dm_room_out_users",
      joinColumns = @JoinColumn(name = "dm_room_id")
  )
  @Column(name = "user_id")
  private Set<UUID> outUsers = new HashSet<>();

  @OneToMany(mappedBy = "dmRoom", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Dm> messages = new ArrayList<>();

  // 기본 생성자 & 생성자
  protected DmRoom() {}

  public DmRoom(UUID senderId, UUID receiverId) {
    this.senderId = senderId;
    this.receiverId = receiverId;
  }
  public void touchLastMessageAt(LocalDateTime lastMessageAt) {
    this.lastMessageAt = lastMessageAt;
  }
  /*public boolean nobodyInRoom() {
    if( senderId == null && receiverId == null ) {
      return true;
    }
    return false;
  }*/
  // 채팅 방 관련 메서드
  public void addOutUser(UUID user){
    outUsers.add(user);
  }
  public void removeOutUser(UUID user){
    outUsers.remove(user);
  }
  public boolean isOut(UUID userId) {
    return outUsers.contains(userId);
  }
  //테스트용 생성자
  public DmRoom(UUID id, UUID senderId, UUID receiverId) {
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
  }
}
