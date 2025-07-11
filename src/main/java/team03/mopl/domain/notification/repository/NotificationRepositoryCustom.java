package team03.mopl.domain.notification.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team03.mopl.domain.notification.entity.Notification;
import team03.mopl.domain.notification.entity.QNotification;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryCustom {

  private final EntityManager em;
  private final JPAQueryFactory queryFactory;

  public List<Notification> findByCursor(UUID userId, int size, String mainCursorValue, String subCursorValue) {
    QNotification notification = QNotification.notification;
    BooleanExpression baseCondition = notification.receiverId.eq(userId);
    if (mainCursorValue != null && subCursorValue != null) {
      LocalDateTime mainCursorDate = LocalDateTime.parse(mainCursorValue); // cursor 기준
      UUID subCursorId = UUID.fromString(subCursorValue); // 서브 기준
      BooleanExpression cursorCondition = notification.createdAt.lt(mainCursorDate).or(notification.createdAt.eq(mainCursorDate).and(notification.id.lt(subCursorId)));

      baseCondition = baseCondition.and(cursorCondition);
    }
    return queryFactory.selectFrom(notification).where(baseCondition).orderBy(notification.createdAt.desc(), notification.id.desc()).limit(size)
        .fetch();
  }

}
