package team03.mopl.domain.content.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.content.Content;

public interface ContentRepository extends JpaRepository<Content, UUID> {

  @Query("SELECT c FROM Content c WHERE " +
  "LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
  "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
  List<Content> findByKeywordMatching(@Param("keyword") String keyword);

  @Query("SELECT c FROM Content c ORDER BY c.avgRating DESC, c.viewCount DESC")
  List<Content> findTopByOrderByAvgRatingDescViewingCountDesc();

}
