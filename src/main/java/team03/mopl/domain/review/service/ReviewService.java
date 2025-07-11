package team03.mopl.domain.review.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;

public interface ReviewService {

  ReviewDto create(ReviewCreateRequest request);

  ReviewDto update(UUID reviewId, ReviewUpdateRequest request, UUID userId);

  ReviewDto get(UUID reviewId);

  List<ReviewDto> getAllByUser(UUID userId);

  List<ReviewDto> getAllByContent(UUID contentId);

  void delete(UUID reviewId, UUID userId);

}
