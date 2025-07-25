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

  /**
   * 키워드에 매핑된 콘텐츠 ID 목록 조회 (중복 방지용)
   * - CurationService.matchNewContentsWithKeyword()에서 사용
   */
  @Query("SELECT kc.content.id FROM KeywordContent kc WHERE kc.keyword = :keyword")
  List<UUID> findContentIdsByKeyword(@Param("keyword") Keyword keyword);
}
