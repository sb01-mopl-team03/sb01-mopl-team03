package team03.mopl.domain.subscription;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubscription is a Querydsl query type for Subscription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscription extends EntityPathBase<Subscription> {

    private static final long serialVersionUID = 1048504390L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubscription subscription = new QSubscription("subscription");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final team03.mopl.domain.playlist.QPlaylist playlist;

    public final team03.mopl.domain.user.QUser user;

    public QSubscription(String variable) {
        this(Subscription.class, forVariable(variable), INITS);
    }

    public QSubscription(Path<? extends Subscription> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubscription(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubscription(PathMetadata metadata, PathInits inits) {
        this(Subscription.class, metadata, inits);
    }

    public QSubscription(Class<? extends Subscription> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.playlist = inits.isInitialized("playlist") ? new team03.mopl.domain.playlist.QPlaylist(forProperty("playlist"), inits.get("playlist")) : null;
        this.user = inits.isInitialized("user") ? new team03.mopl.domain.user.QUser(forProperty("user")) : null;
    }

}

