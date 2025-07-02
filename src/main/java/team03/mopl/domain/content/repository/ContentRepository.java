package team03.mopl.domain.content.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.content.Content;

public interface ContentRepository extends JpaRepository<Content, UUID> {

}
