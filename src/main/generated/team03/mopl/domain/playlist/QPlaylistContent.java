package team03.mopl.domain.playlist;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlaylistContent is a Querydsl query type for PlaylistContent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlaylistContent extends EntityPathBase<PlaylistContent> {

    private static final long serialVersionUID = 606118195L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPlaylistContent playlistContent = new QPlaylistContent("playlistContent");

    public final team03.mopl.domain.content.QContent content;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final QPlaylist playlist;

    public QPlaylistContent(String variable) {
        this(PlaylistContent.class, forVariable(variable), INITS);
    }

    public QPlaylistContent(Path<? extends PlaylistContent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPlaylistContent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPlaylistContent(PathMetadata metadata, PathInits inits) {
        this(PlaylistContent.class, metadata, inits);
    }

    public QPlaylistContent(Class<? extends PlaylistContent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new team03.mopl.domain.content.QContent(forProperty("content")) : null;
        this.playlist = inits.isInitialized("playlist") ? new QPlaylist(forProperty("playlist"), inits.get("playlist")) : null;
    }

}

