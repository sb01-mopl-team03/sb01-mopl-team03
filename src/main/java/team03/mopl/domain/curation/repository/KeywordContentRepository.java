package team03.mopl.domain.curation.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.curation.entity.KeywordContent;

public interface KeywordContentRepository extends JpaRepository<KeywordContent, UUID> {

  List<KeywordContent> findByKeywordId(UUID keywordId);

  boolean existsByKeywordIdAndContentId(UUID keywordId, UUID contentId);
}
