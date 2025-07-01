package team03.mopl.domain.content;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, UUID> {

  boolean existsByContentType(ContentType contentType);

  boolean existsByDataId(String dataId);
}
