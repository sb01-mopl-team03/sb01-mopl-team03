package team03.mopl.domain.review.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.review.ReviewDeleteDeniedException;
import team03.mopl.common.exception.review.ReviewNotFoundException;
import team03.mopl.common.exception.review.ReviewUpdateDeniedException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewDto;
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
  public ReviewDto create(ReviewCreateRequest request) {

    if (!userRepository.existsById(request.userId())) {
      log.warn("존재하지 않는 유저입니다. 유저 ID: ", request.userId());
      throw new UserNotFoundException();
    }

    if (!contentRepository.existsById(request.contentId())) {
      log.warn("존재하지 않는 콘텐츠입니다. 콘텐츠 ID: ", request.contentId());
      throw new ContentNotFoundException();
    }

    User user = userRepository.findById(request.userId()).orElseThrow(UserNotFoundException::new);
    Content content = contentRepository.findById(request.contentId()).orElseThrow(ContentNotFoundException::new);
    Review review = Review.builder()
        .user(user)
        .content(content)
        .title(request.title())
        .comment(request.comment())
        .rating(request.rating())
        .build();
    return ReviewDto.from(reviewRepository.save(review));
  }

  @Override
  public ReviewDto update(UUID reviewId, ReviewUpdateRequest request, UUID userId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
    if (!review.getUser().getId().equals(userId)) {
      log.warn("리뷰 작성자만 수정할 수 있습니다. 리뷰 작성자 ID: ", userId);
      throw new ReviewUpdateDeniedException();
    }

    review.update(request.title(), request.comment(), request.rating());
    return ReviewDto.from(reviewRepository.save(review));
  }

  @Override
  public ReviewDto get(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);

    return ReviewDto.from(review);
  }

  @Override
  public List<ReviewDto> getAllByUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      log.warn("존재하지 않는 유저입니다. 유저 ID: ", userId);
      throw new UserNotFoundException();
    }

    List<Review> reviewList = reviewRepository.findAllByUserId(userId);
    return reviewList.stream().map(ReviewDto::from).toList();
  }

  @Override
  public List<ReviewDto> getAllByContent(UUID contentId) {
    if (!contentRepository.existsById(contentId)) {
      log.warn("존재하지 않는 콘텐츠입니다. 콘텐츠 ID: ", contentId);
      throw new ContentNotFoundException();
    }

    List<Review> reviewList = reviewRepository.findAllByContentId(contentId);
    return reviewList.stream().map(ReviewDto::from).toList();
  }

  @Override
  public void delete(UUID reviewId, UUID userId) {
    Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewNotFoundException::new);
    if (!review.getUser().getId().equals(userId)) {
      log.warn("리뷰 작성자만 삭제할 수 있습니다. 리뷰 작성자 ID: ", userId);
      throw new ReviewDeleteDeniedException();
    }

    reviewRepository.delete(review);
  }
}
