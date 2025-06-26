package team03.mopl.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.user.User;

@Getter
@Entity
@Table(name = "reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "content_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Review {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "content_id")
  private Content content;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(columnDefinition = "TEXT")
  private String comment;

  @DecimalMin(value = "0.0", message = "별점은 0.0 이상이어야 합니다")
  @DecimalMax(value = "5.0", message = "별점은 5.0 이하여야 합니다")
  @Digits(integer = 1, fraction = 1, message = "별점은 소수점 한자리까지만 입력 가능합니다")
  @Column(nullable = false, precision = 2, scale = 1)
  private BigDecimal rating;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public Review(User user, Content content, String title, String comment, BigDecimal rating) {
    this.user = user;
    this.content = content;
    this.title = title;
    this.comment = comment;
    this. rating = rating;
  }

  public void update(String newTitle, String newComment, BigDecimal newRating) {

    if (!newTitle.equals(title)) {
      this.title = newTitle;
    }
    if (!newComment.equals(comment)) {
      this.comment = newComment;
    }
    if (!Objects.equals(newRating, rating)) {
      this.rating = newRating;
    }
  }

}
