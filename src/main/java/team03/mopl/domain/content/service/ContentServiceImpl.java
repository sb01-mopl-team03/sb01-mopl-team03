package team03.mopl.domain.content.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.service.ReviewService;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

  private final ContentRepository contentRepository;
  private final ReviewService reviewService;

  @Override
  public void updateContentRating(UUID contentId) {
    BigDecimal averageRating = calculateRating(contentId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(ContentNotFoundException::new);

    content.setAvgRating(averageRating);
    contentRepository.save(content);
  }

  // review 평점의 평균값 게산
  private BigDecimal calculateRating(UUID contentId) {
    List<ReviewResponse> reviews = reviewService.findAllByContent(contentId);
    if (reviews == null || reviews.isEmpty()) {
      return BigDecimal.ZERO;
    }

    BigDecimal sum = reviews.stream()
        .map(ReviewResponse::rating)
        .filter(Objects::nonNull) // null 값 제외
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return sum.divide(new BigDecimal(reviews.size()), 2, RoundingMode.HALF_UP);
  }

}
