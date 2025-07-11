package team03.mopl.domain.review.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.review.entity.Review;
import team03.mopl.domain.user.User;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

  List<Review> findAllByUserId(UUID userId);

  List<Review> findAllByContentId(UUID contentId);

  boolean existsByUserIdAndContentId(UUID userId, UUID contentId);

  void deleteAllByUserId(UUID userId);
}
