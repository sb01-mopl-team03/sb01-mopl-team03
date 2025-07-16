package team03.mopl.domain.content.repository;

import static team03.mopl.domain.content.QContent.content;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team03.mopl.domain.content.Content;

@Repository
@RequiredArgsConstructor
public class ContentRepositoryImpl implements ContentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  /**
   * 컨텐츠 데이터를 조회합니다. 커서 기반의 페이지네이션을 지원합니다.
   *
   * @param title       필터링 조건, 부분 일치 가능
   * @param contentType 필터링 조건, SPORTS, MOVIE, TV
   * @param sortBy      정렬 기준, TITLE(기본값), RELEASE_AT
   * @param direction   정렬 방향, DESC(기본값), ASC
   * @param cursor      메인 커서가 되는 title_normalized, release_at 값
   * @param cursorId    보조 커서, 컨텐츠의 id 값
   * @param size        한 페이지에 조회할 컨텐츠 개수
   * @return 커서 페이지네이션 기반 Content 리스트
   */
  @Override
  public List<Content> findContentsWithCursor(String title, String contentType, String sortBy,
      String direction, String cursor, UUID cursorId, int size) {

    // 1. 정렬 방향 결정
    boolean isDesc = "DESC".equalsIgnoreCase(direction);
    Order orderDirection = isDesc ? Order.DESC : Order.ASC;
    // 2. 정렬 기준에 따른 OrderSpecifier 설정 : 기준값 기본은 title
    OrderSpecifier<?> mainSort = new OrderSpecifier<>(orderDirection, content.titleNormalized);
    if ("RELEASE_AT".equalsIgnoreCase(sortBy)) {
      mainSort = new OrderSpecifier<>(orderDirection, content.releaseDate);
    } else if ("AVG_RATING".equalsIgnoreCase(sortBy)) {
      mainSort = new OrderSpecifier<>(orderDirection, content.avgRating);
    }
    OrderSpecifier<?> idSort = new OrderSpecifier<>(orderDirection, content.id);

    // 3. 필터링 조건 조립
    BooleanExpression titleExpression =
        title != null && !title.isEmpty() ? content.title.containsIgnoreCase(title) : null;
    BooleanExpression contentTypeExpression =
        contentType != null && !contentType.isEmpty() ? content.contentType.stringValue()
            .equalsIgnoreCase(contentType) : null;

    // 4. 정렬, 필터링 이후 커서 위치 확인
    BooleanExpression cursorExpression = cursorCondition(cursor, cursorId, sortBy, isDesc);

    return queryFactory
        .selectFrom(content)
        .where(
            // 필터링 조건
            titleExpression,
            contentTypeExpression,
            // 커서 조건
            cursorExpression
        )
        // 정렬 적용
        .orderBy(mainSort, idSort)
        // 반환되는 데이터 개수
        .limit(size)
        .fetch();
  }

  /**
   * 커서 조건 설정
   *
   * @param cursor   메인 커서, SortBy 기준과 동일한 종류의 값이 들어온다.
   * @param cursorId 보조 커서, id 값이다. id는 항상 오름차순으롲 정리되어 있으며 중복 cursor에 대비한다.
   * @param sortBy   정렬 기준
   * @param isDesc   내림차순 여부
   */
  private BooleanExpression cursorCondition(String cursor, UUID cursorId, String sortBy,
      boolean isDesc) {
    // 첫 페이지 조회
    if (cursor == null || cursorId == null) {
      return null;
    }

    // 정렬 기준(SortBy)에 따라 분기
    if (sortBy.equalsIgnoreCase("RELEASE_AT")) {
      LocalDateTime localDateTimeCursor = LocalDateTime.parse(cursor);
      if (isDesc) {
        // 내림차순: 커서 일자보다 이전 일자이되 동일한 일자일시 id가 더 큰 값들
        return content.releaseDate.lt(localDateTimeCursor)
            .or(content.releaseDate.eq(localDateTimeCursor).and(content.id.lt(cursorId)));
      } else {
        // 오름차순: 커서 일자보다 이후 일자이되 동일한 일자일시 id가 더 큰 값들
        return content.releaseDate.gt(localDateTimeCursor)
            .or(content.releaseDate.eq(localDateTimeCursor).and(content.id.gt(cursorId)));
      }
    } else if (sortBy.equalsIgnoreCase("TITLE")) {
      if (isDesc) {
        // 내림차순: 커서 제목보다 사전적으로 이전 제목이되 동일한 제목일시 id가 더 큰 값들 
        return content.titleNormalized.lt(cursor)
            .or(content.titleNormalized.eq(cursor).and(content.id.lt(cursorId)));
      } else {
        // 오름차순: 커서 제목보다 사전적으로 이후 제목이되 동일한 제목일시 id가 더 큰 값들
        return content.titleNormalized.gt(cursor)
            .or(content.titleNormalized.eq(cursor).and(content.id.gt(cursorId)));
      }
    } else { //AVG_RATING
      BigDecimal bigDecimalCursor = new BigDecimal(cursor);
      if (isDesc) {
        return content.avgRating.lt(bigDecimalCursor)
            .or(content.avgRating.eq(bigDecimalCursor).and(content.id.lt(cursorId)));
      } else {
        return content.avgRating.gt(bigDecimalCursor)
            .or(content.avgRating.eq(bigDecimalCursor).and(content.id.gt(cursorId)));
      }
    }
  }
}
