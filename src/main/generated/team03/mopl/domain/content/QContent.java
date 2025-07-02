package team03.mopl.domain.content;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QContent is a Querydsl query type for Content
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContent extends EntityPathBase<Content> {

    private static final long serialVersionUID = 1581606844L;

    public static final QContent content = new QContent("content");

    public final NumberPath<java.math.BigDecimal> avgRating = createNumber("avgRating", java.math.BigDecimal.class);

    public final EnumPath<ContentType> contentType = createEnum("contentType", ContentType.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath dataId = createString("dataId");

    public final StringPath description = createString("description");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final DateTimePath<java.time.LocalDateTime> releaseDate = createDateTime("releaseDate", java.time.LocalDateTime.class);

    public final StringPath title = createString("title");

    public final StringPath url = createString("url");

    public QContent(String variable) {
        super(Content.class, forVariable(variable));
    }

    public QContent(Path<? extends Content> path) {
        super(path.getType(), path.getMetadata());
    }

    public QContent(PathMetadata metadata) {
        super(Content.class, metadata);
    }

}

