package team03.mopl.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;
import team03.mopl.common.config.JpaConfig;
import team03.mopl.common.config.QueryDslConfig;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.NotificationType;

@DataJpaTest
@Import({QueryDslConfig.class, JpaConfig.class})
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
    LocalDateTime baseTime = LocalDateTime.of(2025, 7, 19, 12, 0);

    for (int i = 0; i < 10; i++) {
      Notification notification = new Notification(userId, NotificationType.FOLLOWED, "알림 " + i);
      ReflectionTestUtils.setField(notification, "createdAt", baseTime.minusSeconds(i));
      em.persist(notification);
    }
    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("findByCursor_초기_페이지_조회")
  void findByCursor_init() {
    List<Notification> result = notificationRepositoryCustom.findByCursor(userId, 5, null, null);
    assertThat(result).hasSize(5);

    for (int i = 0; i < result.size() - 1; i++) {
      Notification current = result.get(i);
      Notification next = result.get(i + 1);

      boolean isOrdered;

      if (current.getCreatedAt().isAfter(next.getCreatedAt())) {
        isOrdered = true;
      } else if (current.getCreatedAt().isEqual(next.getCreatedAt())) {
        isOrdered = current.getId().compareTo(next.getId()) < 0;
        System.out.println("isOrdered = " + isOrdered);
      } else {
        isOrdered = false;
      }

      assertThat(isOrdered)
          .as("정렬 순서 확인 실패: \n현재 = %s (%s)\n다음 = %s (%s)",
              current.getCreatedAt(), current.getId(),
              next.getCreatedAt(), next.getId())
          .isTrue();
    }


  }

  @Test
  @DisplayName("findByCursor_커서_기반_페이지_조회")
  void findByCursor_withCursor() {
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
}

