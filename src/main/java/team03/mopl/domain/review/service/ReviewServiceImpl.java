package team03.mopl.domain.review.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.review.ReviewNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.entity.Review;
import team03.mopl.domain.review.repository.ReviewRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

  private final ReviewRepository reviewRepository;
  private final UserRepository userRepository;
  private final ContentRepository contentRepository;

  @Override
  public ReviewResponse create(ReviewCreateRequest request) {

    if (!userRepository.existsById(request.userId())) {
      log.debug("존재하지 않는 유저입니다. 유저 ID: ", request.userId());
      throw new UserNotFoundException();
    }

    if (!contentRepository.existsById(request.contentId())) {
      log.debug("존재하지 않는 콘텐츠입니다. 콘텐츠 ID: ", request.contentId());
      throw new ContentNotFoundException();
    }

    User user = userRepository.findById(request.userId()).orElseThrow(UserNotFoundException::new);
    Content content = contentRepository.findById(request.contentId()).orElseThrow(ContentNotFoundException::new);
    Review review = Review.builder().user(user)
            .content(content)
        .title(request.title())
        .comment(request.comment())
        .rating(request.rating()).build();
    return ReviewResponse.from(reviewRepository.save(review));
  }

  @Override
  public ReviewResponse update(UUID reviewId, ReviewUpdateRequest request) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

    review.update(request.title(), request.comment(), request.rating());
    return ReviewResponse.from(reviewRepository.save(review));
  }

  @Override
  public ReviewResponse find(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

    return ReviewResponse.from(review);
  }

  @Override
  public List<ReviewResponse> findAllByUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      log.debug("존재하지 않는 유저입니다. 유저 ID: ", userId);
      throw new UserNotFoundException();
    }

    List<Review> reviewList = reviewRepository.findAllByUserId(userId);
    return reviewList.stream().map(ReviewResponse::from).toList();
  }

  @Override
  public List<ReviewResponse> findAllByContent(UUID contentId) {
    if (!contentRepository.existsById(contentId)) {
      log.debug("존재하지 않는 콘텐츠입니다. 콘텐츠 ID: ", contentId);
      throw new ContentNotFoundException();
    }

    List<Review> reviewList = reviewRepository.findAllByContentId(contentId);
    return reviewList.stream().map(ReviewResponse::from).toList();
  }

  @Override
  public void delete(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
    reviewRepository.delete(review);
  }

  @Override
  public void deleteAllByUser(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    reviewRepository.deleteAllByUserId(userId);
  }
}
