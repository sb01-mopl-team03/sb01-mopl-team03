package team03.mopl.domain.review.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.review.entity.Review;
import team03.mopl.domain.user.User;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  Optional<Review> findByUserId(UUID userId);

  List<Review> findAllByUserId(UUID userId);

  @EntityGraph(attributePaths = {"user","content"})
  List<Review> findAllByContentId(UUID contentId);

  boolean existsByUserIdAndContentId(UUID userId, UUID contentId);

  void deleteAllByUserId(UUID userId);
}
