package team03.mopl.domain.watchroom.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team03.mopl.common.dto.Cursor;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithHeadcountDto;
import team03.mopl.domain.watchroom.dto.WatchRoomSearchInternalDto;
import team03.mopl.domain.watchroom.entity.QWatchRoom;
import team03.mopl.domain.watchroom.entity.QWatchRoomParticipant;
import team03.mopl.domain.content.QContent;

@Repository
@RequiredArgsConstructor
public class WatchRoomParticipantRepositoryImpl implements WatchRoomParticipantRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QWatchRoom qWatchRoom = QWatchRoom.watchRoom;
  private final QContent qContent = QContent.content;
  private final QWatchRoomParticipant qWatchRoomParticipant = QWatchRoomParticipant.watchRoomParticipant;

  @Override
  public List<WatchRoomContentWithHeadcountDto> getAllWatchRoomContentWithHeadcountDto() {
    return queryFactory
        .select(Projections.constructor(WatchRoomContentWithHeadcountDto.class,
            qWatchRoom,
            qWatchRoom.content,
            qWatchRoomParticipant.countDistinct()))
        .from(qWatchRoomParticipant)
        .join(qWatchRoomParticipant.watchRoom, qWatchRoom)
        .join(qWatchRoom.content, qContent).fetchJoin()
        .groupBy(qWatchRoom.id, qContent.id)
        .fetch();
  }

  @Override
  public List<WatchRoomContentWithHeadcountDto> getAllWatchRoomContentWithHeadcountDtoPaginated(
      WatchRoomSearchInternalDto request) {

    BooleanBuilder whereClause = new BooleanBuilder();

    // 검색어 조건
    applySearchKeywordCondition(whereClause, request.getSearchKeyword());

    // 커서 조건
    applyCursorCondition(whereClause, request.getCursor(),
        request.getDirection(), request.getSortBy());

    // 정렬 조건
    OrderSpecifier<?>[] orderSpecifier = getOrderSpecifier(request.getSortBy(),
        request.getDirection());

    return queryFactory
        .select(Projections.constructor(WatchRoomContentWithHeadcountDto.class,
            qWatchRoom,
            qWatchRoom.content,
            qWatchRoomParticipant.countDistinct()))
        .from(qWatchRoom)
        .leftJoin(qWatchRoomParticipant).on(qWatchRoomParticipant.watchRoom.eq(qWatchRoom))
        .join(qWatchRoom.content, qContent)
        .where(whereClause)
        .groupBy(qWatchRoom.id, qContent.id)
        .orderBy(orderSpecifier)
        .limit(request.getSize())
        .fetch();
  }

  private void applySearchKeywordCondition(BooleanBuilder whereClause, String searchKeyword) {
    if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
      return; // null 체크 추가
    }
    // 검색어 조건
    String likePattern = "%" + searchKeyword + "%";

    // 시청방 제목
    BooleanExpression watchRoomTitleCondition = qWatchRoom.title.toLowerCase().like(likePattern);

    // 컨텐츠 제목
    BooleanExpression contentTitleCondition = qContent.title.toLowerCase().like(likePattern);

    // 방장 이름
    BooleanExpression ownerNameCondition = qWatchRoom.owner.name.toLowerCase().like(likePattern);

    whereClause.and(watchRoomTitleCondition.or(contentTitleCondition.or(ownerNameCondition)));
  }

  private void applyCursorCondition(BooleanBuilder whereClause, Cursor cursor,
      String direction, String sortBy) {

    if (cursor.lastId() == null || cursor.lastValue() == null) {
      return;
    }

    boolean isDesc = direction.equalsIgnoreCase("desc");

    //정렬조건 분기
    switch (sortBy.toLowerCase()) {
      case "createdat": //생성일
        LocalDateTime cursorCreatedAt = LocalDateTime.parse(cursor.lastValue());
        applyCreatedAtCursor(whereClause, cursorCreatedAt, UUID.fromString(cursor.lastId()), isDesc);
        break;
      case "title":  //시청방 이름
        applyTitleCursor(whereClause, cursor.lastValue(), UUID.fromString(cursor.lastId()), isDesc);
        break;
      case "participantscount":  //시청자 수
        Long cursorParticipantsCount = Long.parseLong(cursor.lastValue());
        applyParticipantsCountCursor(whereClause, cursorParticipantsCount, UUID.fromString(cursor.lastId()), isDesc);
        break;
      default:
        throw new IllegalArgumentException("지원하지 않는 정렬 조건: " + sortBy);
    }
  }

  // 참여자수 커서 적용
  private void applyParticipantsCountCursor(BooleanBuilder whereClause,
      Long cursorParticipantsCount, UUID lastId, boolean isDesc) {

    if (isDesc) {
      whereClause.and(
          qWatchRoomParticipant.countDistinct().lt(cursorParticipantsCount)
              .or(qWatchRoomParticipant.countDistinct().eq(cursorParticipantsCount)
                  .and(qWatchRoom.id.lt(lastId)))
      );
      return;
    }
    whereClause.and(
        qWatchRoomParticipant.countDistinct().gt(cursorParticipantsCount)
            .or(qWatchRoomParticipant.countDistinct().eq(cursorParticipantsCount)
                .and(qWatchRoom.id.gt(lastId)))
    );

  }

  // 제목 정렬 커서 적용
  private void applyTitleCursor(BooleanBuilder whereClause, String cursorTitle, UUID lastId,
      boolean isDesc) {

    if (isDesc) {
      whereClause.and(
          qWatchRoom.title.lt(cursorTitle)
              .or(qWatchRoom.title.eq(cursorTitle).and(qWatchRoom.id.lt(lastId))));
      return;
    }
    whereClause.and(
        qWatchRoom.title.gt(cursorTitle)
            .or(qWatchRoom.title.eq(cursorTitle).and(qWatchRoom.id.gt(lastId))));

  }

  //생성일 커서 적용
  private void applyCreatedAtCursor(BooleanBuilder whereClause, LocalDateTime cursorCreatedAt,
      UUID lastId, boolean isDesc) {

    if (isDesc) {
      whereClause.and(
          //커서 날짜보다 이전
          qWatchRoom.createdAt.lt(cursorCreatedAt)
              //커서 날짜와 같지만 아이디가 적은 것
              .or(qWatchRoom.createdAt.eq(cursorCreatedAt).and(qWatchRoom.id.lt(lastId)))
      );
      return;
    }
    whereClause.and(
        qWatchRoom.createdAt.gt(cursorCreatedAt)
            .or(qWatchRoom.createdAt.eq(cursorCreatedAt).and(qWatchRoom.id.gt(lastId)))
    );
  }

  //정렬 조건 생성
  private OrderSpecifier<?>[] getOrderSpecifier(String sortBy, String direction) {
    boolean isDesc = direction.equalsIgnoreCase("desc");

    OrderSpecifier<?> primarySort = switch (sortBy.toLowerCase()) {
      case "createdat" -> isDesc ? qWatchRoom.createdAt.desc() : qWatchRoom.createdAt.asc();
      case "title" -> isDesc ? qWatchRoom.title.desc() : qWatchRoom.title.asc();
      case "participantscount" -> isDesc ? qWatchRoomParticipant.countDistinct().desc()
          : qWatchRoomParticipant.countDistinct().asc();
      default -> throw new IllegalArgumentException("Unsupported sort field: " + sortBy);
    };

    OrderSpecifier<?> secondarySort = isDesc ? qWatchRoom.id.desc() : qWatchRoom.id.asc();

    return new OrderSpecifier<?>[] { primarySort, secondarySort };
  }


  @Override
  public Optional<WatchRoomContentWithHeadcountDto> getWatchRoomContentWithHeadcountDto(
      UUID watchRoomId) {

    WatchRoomContentWithHeadcountDto result = queryFactory
        .select(Projections.constructor(WatchRoomContentWithHeadcountDto.class,
            qWatchRoom,
            qWatchRoom.content,
            qWatchRoomParticipant.countDistinct()))
        .from(qWatchRoom)
        .leftJoin(qWatchRoomParticipant).on(qWatchRoomParticipant.watchRoom.eq(qWatchRoom))
        .join(qWatchRoom.content, qContent).fetchJoin()
        .where(qWatchRoom.id.eq(watchRoomId))
        .groupBy(qWatchRoom.id)
        .fetchOne();

    return Optional.ofNullable(result);
  }
}
