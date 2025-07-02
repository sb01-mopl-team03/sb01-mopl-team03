package team03.mopl.domain.dm.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDmRoom is a Querydsl query type for DmRoom
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDmRoom extends EntityPathBase<DmRoom> {

    private static final long serialVersionUID = 675277842L;

    public static final QDmRoom dmRoom = new QDmRoom("dmRoom");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final ListPath<Dm, QDm> messages = this.<Dm, QDm>createList("messages", Dm.class, QDm.class, PathInits.DIRECT2);

    public final ComparablePath<java.util.UUID> receiverId = createComparable("receiverId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> senderId = createComparable("senderId", java.util.UUID.class);

    public QDmRoom(String variable) {
        super(DmRoom.class, forVariable(variable));
    }

    public QDmRoom(Path<? extends DmRoom> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDmRoom(PathMetadata metadata) {
        super(DmRoom.class, metadata);
    }

}

