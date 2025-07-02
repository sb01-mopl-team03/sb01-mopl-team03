package team03.mopl.domain.chat.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import team03.mopl.domain.chat.dto.ChatRoomContentWithHeadcountDto;
import team03.mopl.domain.chat.entity.QChatRoom;
import team03.mopl.domain.chat.entity.QChatRoomParticipant;
import team03.mopl.domain.content.QContent;

@Repository
@RequiredArgsConstructor
public class ChatRoomParticipantRepositoryImpl implements ChatRoomParticipantRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private final QChatRoom qChatRoom = QChatRoom.chatRoom;
  private final QContent qContent = QContent.content;
  private final QChatRoomParticipant qChatRoomParticipant = QChatRoomParticipant.chatRoomParticipant;

  @Override
  public List<ChatRoomContentWithHeadcountDto> getAllChatRoomContentWithHeadcountDto() {
    return queryFactory
        .select(Projections.constructor(ChatRoomContentWithHeadcountDto.class,
            qChatRoom,
            qChatRoom.content,
            qChatRoomParticipant.countDistinct()))
        .from(qChatRoomParticipant)
        .join(qChatRoomParticipant.chatRoom, qChatRoom)
        .join(qChatRoom.content, qContent).fetchJoin()
        .groupBy(qChatRoom.id, qContent.id)
        .fetch();
  }
}
