package team03.mopl.domain.content.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;

public interface ContentRepository extends JpaRepository<Content, UUID>, ContentRepositoryCustom {

  boolean existsByContentType(ContentType contentType);

  boolean existsByDataId(String dataId);
}
