package team03.mopl.domain.content;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

@Getter
@Entity
@Table(name = "contents")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Content {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
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

  @Column(name = "avg_rating", precision = 3, scale = 2)
  private BigDecimal avgRating;

  @Column(name = "url")
  private String url;

  public Content(String title, String description, ContentType contentType, LocalDateTime releaseDate, String url) {
    this.title = title;
    this.description = description;
    this.contentType = contentType;
    this.releaseDate = releaseDate;
    this.url = url;
  }

  public void setAvgRating(BigDecimal avgRating) {
    this.avgRating = avgRating;
  }

}
