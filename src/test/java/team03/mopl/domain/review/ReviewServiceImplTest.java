package team03.mopl.domain.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.review.ReviewDeleteDeniedException;
import team03.mopl.common.exception.review.ReviewNotFoundException;
import team03.mopl.common.exception.review.ReviewUpdateDeniedException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.entity.Review;
import team03.mopl.domain.review.event.ReviewEvent;
import team03.mopl.domain.review.repository.ReviewRepository;
import team03.mopl.domain.review.service.ReviewServiceImpl;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 서비스 테스트")
class ReviewServiceImplTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ApplicationEventPublisher applicationEventPublisher;

  @InjectMocks
  private ReviewServiceImpl reviewService;

  // 테스트용 유저
  private UUID userId;
  private User user;

  // 테스트용 콘텐츠
  private UUID contentId;
  private Content content;

  // 테스트용 리뷰
  private UUID reviewId;
  private Review review;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .name("테스트유저")
        .email("test@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    contentId = UUID.randomUUID();
    content = Content.builder()
        .id(contentId)
        .title("테스트콘텐츠")
        .description("테스트용 콘텐츠 입니다.")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .build();

    reviewId = UUID.randomUUID();
    review = Review.builder()
        .id(reviewId)
        .user(user)
        .content(content)
        .title("테스트 리뷰")
        .comment("테스트 리뷰 내용")
        .rating(BigDecimal.valueOf(5))
        .build();
  }

  @Nested
  @DisplayName("리뷰 생성")
  class CreateReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(reviewRepository.save(any(Review.class))).thenReturn(review);

      // when
      ReviewDto result = reviewService.create(request);

      // then
      assertNotNull(result);
      assertEquals(review.getTitle(), result.title());
      assertEquals(review.getComment(), result.comment());
      assertEquals(review.getRating(), result.rating());

      verify(reviewRepository, times(1)).save(any(Review.class));
      verify(applicationEventPublisher, times(1)).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();
      ReviewCreateRequest request = new ReviewCreateRequest(
          randomUserId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class, () -> reviewService.create(request));

      verify(reviewRepository, never()).save(any(Review.class));
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      // given
      UUID randomContentId = UUID.randomUUID();
      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          randomContentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(randomContentId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(ContentNotFoundException.class, () -> reviewService.create(request));

      verify(reviewRepository, never()).save(any(Review.class));
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }
  }

  @Nested
  @DisplayName("리뷰 수정")
  class UpdateReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
      when(reviewRepository.save(any(Review.class))).thenReturn(review);

      // when
      ReviewDto result = reviewService.update(reviewId, request, userId);

      // then
      assertNotNull(result);
      assertEquals("수정된 리뷰 제목", result.title());
      assertEquals("수정된 리뷰 내용", result.comment());
      assertEquals(BigDecimal.valueOf(4), result.rating());

      verify(reviewRepository, times(1)).save(any(Review.class));
      verify(applicationEventPublisher, times(1)).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      // given
      UUID randomReviewId = UUID.randomUUID();
      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      when(reviewRepository.findById(randomReviewId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(ReviewNotFoundException.class, () -> reviewService.update(randomReviewId, request, userId));

      verify(reviewRepository, never()).save(any(Review.class));
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("다른 사용자의 리뷰 수정 시도")
    void failsWhenNotReviewOwner() {
      // given
      UUID otherUserId = UUID.randomUUID();
      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when & then
      assertThrows(ReviewUpdateDeniedException.class, () -> reviewService.update(reviewId, request, otherUserId));

      verify(reviewRepository, never()).save(any(Review.class));
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }
  }

  @Nested
  @DisplayName("리뷰 조회")
  class GetReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when
      ReviewDto result = reviewService.get(reviewId);

      // then
      assertNotNull(result);
      assertEquals(review.getTitle(), result.title());
      assertEquals(review.getComment(), result.comment());
      assertEquals(review.getRating(), result.rating());

      // 조회 시에는 이벤트 발행하지 않음
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      // given
      UUID randomReviewId = UUID.randomUUID();

      when(reviewRepository.findById(randomReviewId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(ReviewNotFoundException.class, () -> reviewService.get(randomReviewId));
    }
  }

  @Nested
  @DisplayName("유저별 리뷰 조회")
  class GetAllByUser {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      Review review2 = Review.builder()
          .id(UUID.randomUUID())
          .user(user)
          .content(content)
          .title("두 번째 리뷰")
          .comment("두 번째 리뷰 내용")
          .rating(BigDecimal.valueOf(3))
          .build();

      List<Review> reviews = List.of(review, review2);

      when(userRepository.existsById(userId)).thenReturn(true);
      when(reviewRepository.findAllByUserId(userId)).thenReturn(reviews);

      // when
      List<ReviewDto> result = reviewService.getAllByUser(userId);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(review.getTitle(), result.get(0).title());
      assertEquals(review2.getTitle(), result.get(1).title());

      // 조회 시에는 이벤트 발행하지 않음
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();

      when(userRepository.existsById(randomUserId)).thenReturn(false);

      // when & then
      assertThrows(UserNotFoundException.class, () -> reviewService.getAllByUser(randomUserId));
    }
  }

  @Nested
  @DisplayName("콘텐츠별 리뷰 조회")
  class GetAllByContent {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      Review review2 = Review.builder()
          .id(UUID.randomUUID())
          .user(user)
          .content(content)
          .title("두 번째 리뷰")
          .comment("두 번째 리뷰 내용")
          .rating(BigDecimal.valueOf(3))
          .build();

      List<Review> reviews = List.of(review, review2);

      when(contentRepository.existsById(contentId)).thenReturn(true);
      when(reviewRepository.findAllByContentId(contentId)).thenReturn(reviews);

      // when
      List<ReviewDto> result = reviewService.getAllByContent(contentId);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(review.getTitle(), result.get(0).title());
      assertEquals(review2.getTitle(), result.get(1).title());

      // 조회 시에는 이벤트 발행하지 않음
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      // given
      UUID randomContentId = UUID.randomUUID();

      when(contentRepository.existsById(randomContentId)).thenReturn(false);

      // when & then
      assertThrows(ContentNotFoundException.class, () -> reviewService.getAllByContent(randomContentId));
    }
  }

  @Nested
  @DisplayName("리뷰 삭제")
  class DeleteReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when
      reviewService.delete(reviewId, userId);

      // then
      verify(reviewRepository, times(1)).delete(review);
      verify(applicationEventPublisher, times(1)).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      // given
      UUID randomReviewId = UUID.randomUUID();

      when(reviewRepository.findById(randomReviewId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(ReviewNotFoundException.class, () -> reviewService.delete(randomReviewId, userId));

      verify(reviewRepository, never()).delete(any(Review.class));
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }

    @Test
    @DisplayName("다른 사용자의 리뷰 삭제 시도")
    void failsWhenNotReviewOwner() {
      // given
      UUID otherUserId = UUID.randomUUID();

      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when & then
      assertThrows(ReviewDeleteDeniedException.class, () -> reviewService.delete(reviewId, otherUserId));

      verify(reviewRepository, never()).delete(any(Review.class));
      verify(applicationEventPublisher, never()).publishEvent(any(ReviewEvent.class));
    }
  }

  @Nested
  @DisplayName("이벤트 발행 테스트")
  class EventPublishTest {

    @Test
    @DisplayName("리뷰 생성 시 created 이벤트 발행")
    void publishesCreatedEventOnCreate() {
      // given
      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(reviewRepository.save(any(Review.class))).thenReturn(review);

      // when
      reviewService.create(request);

      // then
      verify(applicationEventPublisher).publishEvent(
          ReviewEvent.created(contentId)
      );
    }

    @Test
    @DisplayName("리뷰 수정 시 updated 이벤트 발행")
    void publishesUpdatedEventOnUpdate() {
      // given
      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 제목",
          "수정된 내용",
          BigDecimal.valueOf(4)
      );

      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
      when(reviewRepository.save(any(Review.class))).thenReturn(review);

      // when
      reviewService.update(reviewId, request, userId);

      // then
      verify(applicationEventPublisher).publishEvent(
          ReviewEvent.updated(contentId)
      );
    }

    @Test
    @DisplayName("리뷰 삭제 시 deleted 이벤트 발행")
    void publishesDeletedEventOnDelete() {
      // given
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      // when
      reviewService.delete(reviewId, userId);

      // then
      verify(applicationEventPublisher).publishEvent(
          ReviewEvent.deleted(contentId)
      );
    }
  }
}
