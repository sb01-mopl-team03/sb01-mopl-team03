package team03.mopl.domain.dm.entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@Table(name = "dm_rooms")
public class DmRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "sender_id")
  private UUID senderId;

  @Column(name = "recevier_id")
  private UUID receiverId;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "dmRoom", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Dm> messages = new ArrayList<>();

  //== 연관 관계 편의 메서드==//
  public void addMessage(Dm dm) {
    messages.add(dm);
    dm.setDmRoom(this);
  }

  // 기본 생성자 & 생성자
  protected DmRoom() {}

  public DmRoom(UUID senderId, UUID receiverId) {
    this.senderId = senderId;
    this.receiverId = receiverId;
  }
}
