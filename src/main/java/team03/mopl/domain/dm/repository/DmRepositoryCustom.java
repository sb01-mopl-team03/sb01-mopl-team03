package team03.mopl.domain.dm.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.QDm;

@Repository
@RequiredArgsConstructor
public class DmRepositoryCustom {

  private final EntityManager em;
  private final JPAQueryFactory queryFactory;

  public List<Dm> findByCursor(UUID roomId, int size, String mainCursorValue, String subCursorValue) {
    QDm dm = QDm.dm;

    BooleanExpression baseCondition = dm.dmRoom.id.eq(roomId);

    // 커서 조건
    if (mainCursorValue != null && subCursorValue != null) {
      LocalDateTime mainCursorDate = LocalDateTime.parse(mainCursorValue); // cursor 기준
      UUID subCursorId = UUID.fromString(subCursorValue); // 서브 기준

      BooleanExpression cursorCondition = dm.createdAt.lt(mainCursorDate).or(dm.createdAt.eq(mainCursorDate).and(dm.id.lt(subCursorId)));

      baseCondition = baseCondition.and(cursorCondition);
    }

    return queryFactory.selectFrom(dm).where(baseCondition).orderBy(dm.createdAt.desc(), dm.id.desc()).limit(size).fetch();
  }

}
