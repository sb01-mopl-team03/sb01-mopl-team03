package team03.mopl.domain.curation.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.curation.entity.KeywordContent;

public interface KeywordContentRepository extends JpaRepository<KeywordContent, UUID> {

  List<KeywordContent> findByKeywordId(UUID keywordId);

  boolean existsByKeywordIdAndContentId(UUID keywordId, UUID contentId);

  @Query(value = """
      SELECT * FROM keyword_contents kc 
      WHERE kc.keyword_id = :keywordId 
      ORDER BY kc.score DESC, kc.content_id ASC 
      LIMIT 30
      """, nativeQuery = true)
  List<KeywordContent> findTop30ByKeywordIdOrderByScoreDesc(
      @Param("keywordId") UUID keywordId
  );

  // 키워드별 점수 존재 여부 확인
  boolean existsByKeywordId(@Param("keywordId") UUID keywordId);

  // 키워드별 총 추천 콘텐츠 수
  @Query("SELECT COUNT(kc) FROM KeywordContent kc WHERE kc.keyword.id = :keywordId")
  long countByKeywordId(@Param("keywordId") UUID keywordId);

  // 특정 키워드의 모든 점수 삭제 (재계산 시 사용)
  @Modifying
  @Query("DELETE FROM KeywordContent kc WHERE kc.keyword.id = :keywordId")
  void deleteByKeywordId(@Param("keywordId") UUID keywordId);

}
