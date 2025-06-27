package team03.mopl.domain.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Entity
@Table(name = "contents")
public class Content {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "description")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Column(name = "release_date", nullable = false)
  private LocalDateTime releaseDate;

  public Content(String title, String description, ContentType contentType, LocalDateTime releaseDate) {
    this.title = title;
    this.description = description;
    this.contentType = contentType;
    this.releaseDate = releaseDate;
  }

}
