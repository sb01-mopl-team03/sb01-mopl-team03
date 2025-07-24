package team03.mopl.domain.curation.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.entity.KeywordContent;

public interface KeywordContentRepository extends JpaRepository<KeywordContent, UUID> {

  /**
   * 키워드별 콘텐츠 조회 (점수 내림차순)
   * - CurationService.getRecommendationsByKeyword()에서 사용
   */
  List<KeywordContent> findByKeywordOrderByScoreDesc(Keyword keyword);

  boolean existsByKeywordIdAndContentId(UUID keywordId, UUID contentId);

  /**
   * 키워드에 매핑된 콘텐츠 ID 목록 조회 (중복 방지용)
   * - CurationService.matchNewContentsWithKeyword()에서 사용
   */
  @Query("SELECT kc.content.id FROM KeywordContent kc WHERE kc.keyword = :keyword")
  List<UUID> findContentIdsByKeyword(@Param("keyword") Keyword keyword);

  List<KeywordContent> findByKeywordIdAndScoreGreaterThanEqualOrderByScoreDesc(
      UUID keywordId, double score);

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
