package team03.mopl.domain.follow.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFollow is a Querydsl query type for Follow
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFollow extends EntityPathBase<Follow> {

    private static final long serialVersionUID = -286832665L;

    public static final QFollow follow = new QFollow("follow");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ComparablePath<java.util.UUID> followerId = createComparable("followerId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> followingId = createComparable("followingId", java.util.UUID.class);

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public QFollow(String variable) {
        super(Follow.class, forVariable(variable));
    }

    public QFollow(Path<? extends Follow> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFollow(PathMetadata metadata) {
        super(Follow.class, metadata);
    }

}

