package team03.mopl.domain.watchroom.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import team03.mopl.domain.content.Content;


@Entity
@Table(name = "watch_rooms")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WatchRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "owner_id", nullable = false)
  private UUID ownerId;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToOne
  @JoinColumn(name = "content_id", nullable = false)
  private Content content;

  @Column(name = "play_time")
  @Builder.Default
  private Double playTime = 0.0;

  @Column(name = "is_playing")
  @Builder.Default
  private Boolean isPlaying = false;

  @Column(name = "video_state_updated_at")
  @Builder.Default
  private LocalDateTime videoStateUpdatedAt = LocalDateTime.now();

  //재생
  public void play() {
    this.isPlaying = true;
    this.videoStateUpdatedAt = LocalDateTime.now();
  }

  //일시정지
  public void pause() {
    this.playTime = calculateRealPlayTime();
    this.isPlaying = false;
    this.videoStateUpdatedAt = LocalDateTime.now();
  }

  // 특정 시간대로 이동
  public void seekTo(double newTime) {
    this.playTime = newTime;
    this.videoStateUpdatedAt = LocalDateTime.now();
  }

  //경과 시간 계산
  public double calculateRealPlayTime() {
    if (!this.isPlaying || this.videoStateUpdatedAt == null) {
      return this.playTime;
    }

    LocalDateTime now = LocalDateTime.now();
    Duration elapsed = Duration.between(this.videoStateUpdatedAt, now);
    double elapsedSeconds = elapsed.toMillis() / 1000.0;

    return this.playTime + elapsedSeconds;
  }

  //방장 변경
  public void changeOwner(UUID newOwnerId) {
    this.ownerId = newOwnerId;
  }

  //방장인지 확인
  public boolean isOwner(UUID userId) {
    return this.ownerId.equals(userId);
  }
}
