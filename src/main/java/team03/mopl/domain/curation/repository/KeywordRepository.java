package team03.mopl.domain.curation.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team03.mopl.domain.curation.entity.Keyword;

@Repository
public interface KeywordRepository extends JpaRepository<Keyword, UUID> {

  /**
   * 사용자별 키워드 조회 (생성일 내림차순)
   * - CurationService.getKeywordsByUser()에서 사용
   */
  List<Keyword> findByUserIdOrderByCreatedAtDesc(UUID userId);

  /**
   * 키워드 소유권 확인 및 조회
   * - CurationService.getRecommendationsByKeyword()에서 사용
   * - CurationService.delete()에서 사용
   */
  Optional<Keyword> findByIdAndUserId(UUID keywordId, UUID userId);
}
