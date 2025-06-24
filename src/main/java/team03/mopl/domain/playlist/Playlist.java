package team03.mopl.domain.playlist;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team03.mopl.domain.subscription.Subscription;
import team03.mopl.domain.user.User;

@Getter
@Entity
@Table(name = "playlists")
@EntityListeners(AuditingEntityListener.class)
public class Playlist {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "name", length = 100)
  private String name;

  @Column(name = "is_public", nullable = false)
  private boolean isPublic;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "creator_id")
  private User user;

  @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL)
  private List<PlaylistContent> playlistContents;

  @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL)
  private List<Subscription> subscriptions;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

}
