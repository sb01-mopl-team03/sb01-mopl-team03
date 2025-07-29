package team03.mopl.domain.review.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.review.DuplicateReviewException;
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
import team03.mopl.domain.review.event.ReviewEvent;
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
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  @Transactional
  public ReviewDto create(ReviewCreateRequest request) {
    User user = userRepository.findById(request.userId())
        .orElseThrow(UserNotFoundException::new);

    Content content = contentRepository.findById(request.contentId())
        .orElseThrow(() -> {
          log.warn("존재하지 않는 콘텐츠입니다. 콘텐츠 ID = {}", request.contentId());
          return new ContentNotFoundException();
        });

    if (reviewRepository.existsByUserIdAndContentId(user.getId(), content.getId())) {
      log.warn("이미 해당 콘텐츠에 리뷰를 작성했습니다. 콘텐츠 ID = {}, 사용자 ID = {}", user.getId(), content.getId());
      throw new DuplicateReviewException();
    }

    Review review = Review.builder()
        .user(user)
        .content(content)
        .title(request.title())
        .comment(request.comment())
        .rating(request.rating())
        .build();

    Review savedReview = reviewRepository.save(review);

    applicationEventPublisher.publishEvent(
        ReviewEvent.created(savedReview.getContent().getId()));

    return ReviewDto.from(savedReview);
  }

  @Override
  @Transactional
  public ReviewDto update(UUID reviewId, ReviewUpdateRequest request, UUID userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(ReviewNotFoundException::new);

    if (!review.getUser().getId().equals(userId)) {
      log.warn("리뷰 작성자만 수정할 수 있습니다. 요청 유저 ID = {}, 리뷰 작성자 ID = {}",
          userId, review.getUser().getId());
      throw new ReviewUpdateDeniedException();
    }

    review.update(request.title(), request.comment(), request.rating());
    Review savedReview = reviewRepository.save(review);

    applicationEventPublisher.publishEvent(
        ReviewEvent.updated(savedReview.getContent().getId())
    );

    return ReviewDto.from(savedReview);
  }

  @Override
  public ReviewDto get(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(ReviewNotFoundException::new);
    return ReviewDto.from(review);
  }

  @Override
  public List<ReviewDto> getAllByUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      log.warn("존재하지 않는 유저입니다. 유저 ID = {}", userId);
      throw new UserNotFoundException();
    }

    List<Review> reviewList = reviewRepository.findAllByUserId(userId);
    return reviewList.stream().map(ReviewDto::from).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<ReviewDto> getAllByContent(UUID contentId) {
    if (!contentRepository.existsById(contentId)) {
      log.warn("존재하지 않는 콘텐츠입니다. 콘텐츠 ID = {}", contentId);
      throw new ContentNotFoundException();
    }

    List<Review> reviewList = reviewRepository.findAllByContentId(contentId);
    return reviewList.stream().map(ReviewDto::from).toList();
  }

  @Override
  @Transactional
  public void delete(UUID reviewId, UUID userId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(ReviewNotFoundException::new);

    if (!review.getUser().getId().equals(userId)) {
      log.warn("리뷰 작성자만 삭제할 수 있습니다. 요청 유저 ID = {}, 리뷰 작성자 ID = {}",
          userId, review.getUser().getId());
      throw new ReviewDeleteDeniedException();
    }

    UUID contentId = review.getContent().getId(); // 삭제 전에 contentId 저장
    reviewRepository.delete(review);

    // 삭제 완료 후 이벤트 발행
    applicationEventPublisher.publishEvent(
        ReviewEvent.deleted(contentId)
    );
  }
}
