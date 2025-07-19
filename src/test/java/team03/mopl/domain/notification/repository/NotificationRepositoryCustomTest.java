package team03.mopl.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.context.annotation.Import;
import team03.mopl.common.config.QueryDslConfig;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

@DataJpaTest
@Import(QueryDslConfig.class)
class NotificationRepositoryCustomTest {

  @Autowired
  private EntityManager em;

  private NotificationRepositoryCustom notificationRepositoryCustom;

  private UUID userId;

  @BeforeEach
  void setUp() {
    notificationRepositoryCustom = new NotificationRepositoryCustom(em, new JPAQueryFactory(em));
    userId = UUID.randomUUID();

    // 테스트 데이터 삽입 (10분 단위로 생성일 설정)
    for (int i = 0; i < 10; i++) {
      Notification notification = new Notification(userId, NotificationType.FOLLOWED, "알림 " + i);
      em.persist(setCreatedAt(notification, LocalDateTime.now().minusMinutes(i)));
    }
    em.flush();
    em.clear();
  }

  @Test
  void findByCursor_초기_페이지_조회() {
    List<Notification> result = notificationRepositoryCustom.findByCursor(userId, 5, null, null);

    assertThat(result).hasSize(5);
    assertThat(result).isSortedAccordingTo((a, b) -> {
      int compareTime = b.getCreatedAt().compareTo(a.getCreatedAt());
      return compareTime != 0 ? compareTime : b.getId().compareTo(a.getId());
    });
  }

  @Test
  void findByCursor_커서_기반_페이지_조회() {
    List<Notification> firstPage = notificationRepositoryCustom.findByCursor(userId, 5, null, null);
    Notification last = firstPage.get(4); // 커서 기준

    List<Notification> nextPage = notificationRepositoryCustom.findByCursor(
        userId,
        5,
        last.getCreatedAt().toString(),
        last.getId().toString()
    );

    assertThat(nextPage).isNotEmpty();
    for (Notification n : nextPage) {
      //생성일이 더 과거거나,
      // 생성일이 동일할 경우 UUID가 더 작은 경우만 허용
      boolean isOlder =
          n.getCreatedAt().isBefore(last.getCreatedAt()) || (n.getCreatedAt().isEqual(last.getCreatedAt()) && n.getId().compareTo(last.getId()) < 0);
      assertThat(isOlder).isTrue();
    }
  }

  // createdAt 수동 설정 메서드
  private Notification setCreatedAt(Notification n, LocalDateTime createdAt) {
    try {
      var field = Notification.class.getDeclaredField("createdAt");
      field.setAccessible(true);
      field.set(n, createdAt);
      return n;
    } catch (Exception e) {
      throw new RuntimeException("createdAt 필드 설정 실패", e);
    }
  }
}

