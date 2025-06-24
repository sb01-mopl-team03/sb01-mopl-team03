package team03.mopl.domain.playlist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import team03.mopl.domain.content.Content;

@Getter
@Entity
@Table(name = "playlist_contents", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"playlist_id", "content_id"})
})
public class PlaylistContent {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "playlist_id")
  private Playlist playlist;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "content_id")
  private Content content;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;
}
