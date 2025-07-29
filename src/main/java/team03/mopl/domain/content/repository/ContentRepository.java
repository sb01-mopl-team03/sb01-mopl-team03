package team03.mopl.domain.content.repository;

import java.time.LocalDateTime;
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

  boolean existsByTitle(String title);

  @Query("SELECT c FROM Content c WHERE " +
      "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
      "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Content> findByKeyword(@Param("keyword") String keyword);

  // 특정 시간 이후에 생성된 TMDB 콘텐츠 조회
  @Query("SELECT c FROM Content c WHERE c.createdAt > :dateTime AND (c.contentType = 'MOVIE' or c.contentType = 'TV') ORDER BY c.createdAt DESC")
  List<Content> findRecentTmdbContentsAfter(@Param("dateTime") LocalDateTime dateTime);

  // 특정 시간 이후에 생성된 Sports 콘텐츠 조회
  @Query("SELECT c FROM Content c WHERE c.createdAt > :dateTime AND c.contentType = 'SPORTS' ORDER BY c.createdAt DESC")
  List<Content> findRecentSportsContentsAfter(@Param("dateTime") LocalDateTime dateTime);
}
