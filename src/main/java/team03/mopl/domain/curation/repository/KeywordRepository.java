package team03.mopl.domain.curation.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.curation.entity.Keyword;

public interface KeywordRepository extends JpaRepository<Keyword, UUID> {

  List<Keyword> findAllByUserId(UUID userId);
}
