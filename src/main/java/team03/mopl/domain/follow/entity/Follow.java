package team03.mopl.domain.follow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "follows")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Follow {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  //팔로우 하는 사람
  @Column(name = "follower_id", nullable = false)
  private UUID followerId;

  //팔로우 당하는 사람
  @Column(name = "following_id", nullable = false)
  private UUID followingId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Follow(UUID followerId, UUID followingId) {
    this.followerId = followerId;
    this.followingId = followingId;
  }
}
