package team03.mopl.jwt;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QJwtSession is a Querydsl query type for JwtSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJwtSession extends EntityPathBase<JwtSession> {

    private static final long serialVersionUID = -387800456L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QJwtSession jwtSession = new QJwtSession("jwtSession");

    public final StringPath accessToken = createString("accessToken");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath refreshToken = createString("refreshToken");

    public final team03.mopl.domain.user.QUser user;

    public QJwtSession(String variable) {
        this(JwtSession.class, forVariable(variable), INITS);
    }

    public QJwtSession(Path<? extends JwtSession> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QJwtSession(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QJwtSession(PathMetadata metadata, PathInits inits) {
        this(JwtSession.class, metadata, inits);
    }

    public QJwtSession(Class<? extends JwtSession> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new team03.mopl.domain.user.QUser(forProperty("user")) : null;
    }

}

