package team03.mopl.domain.content.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;

public interface ContentRepository extends JpaRepository<Content, UUID>, ContentRepositoryCustom {

  boolean existsByContentType(ContentType contentType);

  boolean existsByDataId(String dataId);

  // 배치 처리를 위한 페이징 조회
  @Query(value = """
        SELECT * FROM content c 
        ORDER BY c.id 
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
  List<Content> findAllWithPagination(@Param("offset") int offset, @Param("limit") int limit);

  @Query("SELECT COUNT(c) FROM Content c WHERE " +
      "(:title IS NULL OR c.titleNormalized LIKE %:title%) AND " +
      "(:contentType IS NULL OR c.contentType = :contentType)")
  long countByTitleAndContentType(@Param("title") String title,
      @Param("contentType") String contentType);
}
