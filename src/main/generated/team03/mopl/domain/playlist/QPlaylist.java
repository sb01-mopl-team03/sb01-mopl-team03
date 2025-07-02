package team03.mopl.domain.playlist;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlaylist is a Querydsl query type for Playlist
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlaylist extends EntityPathBase<Playlist> {

    private static final long serialVersionUID = 2129759334L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPlaylist playlist = new QPlaylist("playlist");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isPublic = createBoolean("isPublic");

    public final StringPath name = createString("name");

    public final ListPath<PlaylistContent, QPlaylistContent> playlistContents = this.<PlaylistContent, QPlaylistContent>createList("playlistContents", PlaylistContent.class, QPlaylistContent.class, PathInits.DIRECT2);

    public final ListPath<team03.mopl.domain.subscription.Subscription, team03.mopl.domain.subscription.QSubscription> subscriptions = this.<team03.mopl.domain.subscription.Subscription, team03.mopl.domain.subscription.QSubscription>createList("subscriptions", team03.mopl.domain.subscription.Subscription.class, team03.mopl.domain.subscription.QSubscription.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final team03.mopl.domain.user.QUser user;

    public QPlaylist(String variable) {
        this(Playlist.class, forVariable(variable), INITS);
    }

    public QPlaylist(Path<? extends Playlist> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPlaylist(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPlaylist(PathMetadata metadata, PathInits inits) {
        this(Playlist.class, metadata, inits);
    }

    public QPlaylist(Class<? extends Playlist> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new team03.mopl.domain.user.QUser(forProperty("user")) : null;
    }

}

