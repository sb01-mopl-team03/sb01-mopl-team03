package team03.mopl.domain.watchroom.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team03.mopl.domain.watchroom.dto.WatchRoomContentWithHeadcountDto;
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
  public List<WatchRoomContentWithHeadcountDto> getAllChatRoomContentWithHeadcountDto() {
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
}
