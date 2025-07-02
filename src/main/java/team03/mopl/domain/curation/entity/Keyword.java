package team03.mopl.domain.curation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import team03.mopl.domain.user.User;

@Getter
@Entity
@Table(name = "keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Keyword {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false, updatable = false)
  private String keyword;

  @CreatedDate
  @Column(nullable = false)
  private LocalDateTime createdAt;

  public Keyword(User user, String keyword) {
    this.user = user;
    this.keyword = keyword;
  }
}
