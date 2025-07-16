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

  // 최근 TMDB 콘텐츠 조회 (예: 최근 1시간 내)
  @Query("SELECT c FROM Content c WHERE c.createdAt >= :since AND c.contentType IN ('MOVIE', 'TV')")
  List<Content> findRecentTmdbContents(@Param("since") LocalDateTime since);

  default List<Content> findRecentTmdbContents() {
    return findRecentTmdbContents(LocalDateTime.now().minusHours(1));
  }

  // 최근 Sports 콘텐츠 조회
  @Query("SELECT c FROM Content c WHERE c.createdAt >= :since AND c.contentType = 'SPORTS'")
  List<Content> findRecentSportsContents(@Param("since") LocalDateTime since);

  default List<Content> findRecentSportsContents() {
    return findRecentSportsContents(LocalDateTime.now().minusHours(1));
  }

  // 큐레이션 상태 확인용 메서드들
  @Query("SELECT COUNT(DISTINCT c.id) FROM Content c JOIN KeywordContent kc ON c.id = kc.content.id")
  long countCuratedContents();

  @Query("SELECT k.keyword, COUNT(kc), AVG(kc.score) FROM KeywordContent kc JOIN kc.keyword k GROUP BY k.keyword")
  List<Object[]> getKeywordContentStats();

  @Query("SELECT c FROM Content c WHERE c.createdAt >= :since")
  List<Content> findRecentContents(@Param("since") LocalDateTime since);

  default List<Content> findRecentContents(int daysBack) {
    return findRecentContents(LocalDateTime.now().minusDays(daysBack));
  }

  @Query("SELECT c FROM Content c ORDER BY c.id LIMIT :limit OFFSET :offset")
  List<Content> findAllWithOffset(@Param("offset") int offset, @Param("limit") int limit);
}
