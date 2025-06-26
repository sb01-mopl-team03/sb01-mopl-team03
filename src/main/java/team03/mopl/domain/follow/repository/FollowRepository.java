package team03.mopl.domain.follow.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import team03.mopl.domain.follow.entity.Follow;

public interface FollowRepository extends JpaRepository<Follow, UUID> {

  boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

  void deleteByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

  List<Follow> findAllByFollowerId(UUID followerId);
}
