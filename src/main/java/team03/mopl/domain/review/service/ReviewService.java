package team03.mopl.domain.review.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;

public interface ReviewService {

  ReviewResponse create(ReviewCreateRequest request);

  ReviewResponse update(UUID reviewId, ReviewUpdateRequest request);

  ReviewResponse find(UUID reviewId);

  List<ReviewResponse> findAllByUser(UUID userId);

  List<ReviewResponse> findAllByContent(UUID contentId);

  void delete(UUID reviewId);

  void deleteAllByUser(UUID userId);

}
