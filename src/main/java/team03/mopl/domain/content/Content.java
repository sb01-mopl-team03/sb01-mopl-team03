package team03.mopl.domain.content;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team03.mopl.domain.curation.entity.KeywordContent;
import team03.mopl.domain.review.entity.Review;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "contents")
public class Content {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "data_id")
  private String dataId;

  @Column(name = "title", nullable = false)
  private String title;

  // 컨텐츠 목록 조회: title 기준으로 정렬, 반환할 때 → 아스키코드로 인한 원치않은 정렬을 회피할 목적으로 컬럼 생성
  @Column(name = "title_normalized", nullable = false)
  private String titleNormalized;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "content_type", nullable = false)
  private ContentType contentType;

  @Column(name = "release_date", nullable = false)
  private LocalDateTime releaseDate;

  @Column(name = "avg_rating", precision = 3, scale = 2)
  private BigDecimal avgRating;

  @Column(name = "youtube_url", nullable = false)
  private String youtubeUrl;

  @Column(name = "thumbnail_url")
  private String thumbnailUrl;

  @Builder.Default
  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<KeywordContent> keywordContents = new ArrayList<>();

  @Builder.Default
  @OneToMany(mappedBy = "content", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Review> reviews = new ArrayList<>();

  public void setAvgRating(BigDecimal avgRating) {
    this.avgRating = avgRating;
  }

}
