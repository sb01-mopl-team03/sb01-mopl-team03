package team03.mopl.domain.dm.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDm is a Querydsl query type for Dm
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDm extends EntityPathBase<Dm> {

    private static final long serialVersionUID = 1415977623L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDm dm = new QDm("dm");

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QDmRoom dmRoom;

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final SetPath<java.util.UUID, ComparablePath<java.util.UUID>> readUserIds = this.<java.util.UUID, ComparablePath<java.util.UUID>>createSet("readUserIds", java.util.UUID.class, ComparablePath.class, PathInits.DIRECT2);

    public final ComparablePath<java.util.UUID> senderId = createComparable("senderId", java.util.UUID.class);

    public QDm(String variable) {
        this(Dm.class, forVariable(variable), INITS);
    }

    public QDm(Path<? extends Dm> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDm(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDm(PathMetadata metadata, PathInits inits) {
        this(Dm.class, metadata, inits);
    }

    public QDm(Class<? extends Dm> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.dmRoom = inits.isInitialized("dmRoom") ? new QDmRoom(forProperty("dmRoom")) : null;
    }

}

