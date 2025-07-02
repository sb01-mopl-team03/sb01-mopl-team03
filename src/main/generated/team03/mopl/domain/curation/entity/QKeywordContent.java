package team03.mopl.domain.curation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QKeywordContent is a Querydsl query type for KeywordContent
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QKeywordContent extends EntityPathBase<KeywordContent> {

    private static final long serialVersionUID = -444154742L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QKeywordContent keywordContent = new QKeywordContent("keywordContent");

    public final team03.mopl.domain.content.QContent content;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final QKeyword keyword;

    public QKeywordContent(String variable) {
        this(KeywordContent.class, forVariable(variable), INITS);
    }

    public QKeywordContent(Path<? extends KeywordContent> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QKeywordContent(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QKeywordContent(PathMetadata metadata, PathInits inits) {
        this(KeywordContent.class, metadata, inits);
    }

    public QKeywordContent(Class<? extends KeywordContent> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.content = inits.isInitialized("content") ? new team03.mopl.domain.content.QContent(forProperty("content")) : null;
        this.keyword = inits.isInitialized("keyword") ? new QKeyword(forProperty("keyword"), inits.get("keyword")) : null;
    }

}

