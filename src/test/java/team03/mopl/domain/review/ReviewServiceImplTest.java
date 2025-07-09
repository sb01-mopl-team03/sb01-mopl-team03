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
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.review.ReviewNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.entity.Review;
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
      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.existsById(userId)).thenReturn(true);
      when(contentRepository.existsById(contentId)).thenReturn(true);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(reviewRepository.save(any(Review.class))).thenReturn(review);

      ReviewResponse result = reviewService.create(request);

      assertNotNull(result);
      assertEquals(review.getTitle(), result.title());
      assertEquals(review.getComment(), result.comment());
      assertEquals(review.getRating(), result.rating());

      verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      UUID randomUserId = UUID.randomUUID();
      ReviewCreateRequest request = new ReviewCreateRequest(
          randomUserId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.existsById(randomUserId)).thenReturn(false);

      assertThrows(UserNotFoundException.class, () -> reviewService.create(request));

      verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      UUID randomContentId = UUID.randomUUID();
      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          randomContentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.existsById(userId)).thenReturn(true);
      when(contentRepository.existsById(randomContentId)).thenReturn(false);

      assertThrows(ContentNotFoundException.class, () -> reviewService.create(request));

      verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("유저 조회 실패")
    void failsWhenUserFindFailed() {
      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.existsById(userId)).thenReturn(true);
      when(contentRepository.existsById(contentId)).thenReturn(true);
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThrows(UserNotFoundException.class, () -> reviewService.create(request));

      verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("콘텐츠 조회 실패")
    void failsWhenContentFindFailed() {
      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(userRepository.existsById(userId)).thenReturn(true);
      when(contentRepository.existsById(contentId)).thenReturn(true);
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(contentRepository.findById(contentId)).thenReturn(Optional.empty());

      assertThrows(ContentNotFoundException.class, () -> reviewService.create(request));

      verify(reviewRepository, never()).save(any(Review.class));
    }
  }

  @Nested
  @DisplayName("리뷰 수정")
  class UpdateReview {

    @Test
    @DisplayName("성공")
    void success() {
      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      Review updatedReview = Review.builder()
          .id(reviewId)
          .user(user)
          .content(content)
          .title("수정된 리뷰 제목")
          .comment("수정된 리뷰 내용")
          .rating(BigDecimal.valueOf(4))
          .build();

      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
      when(reviewRepository.save(any(Review.class))).thenReturn(updatedReview);

      ReviewResponse result = reviewService.update(reviewId, request);

      assertNotNull(result);
      assertEquals("수정된 리뷰 제목", result.title());
      assertEquals("수정된 리뷰 내용", result.comment());
      assertEquals(BigDecimal.valueOf(4), result.rating());

      verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      UUID randomReviewId = UUID.randomUUID();
      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      when(reviewRepository.findById(randomReviewId)).thenReturn(Optional.empty());

      assertThrows(ReviewNotFoundException.class, () -> reviewService.update(randomReviewId, request));

      verify(reviewRepository, never()).save(any(Review.class));
    }
  }

  @Nested
  @DisplayName("리뷰 조회")
  class FindReview {

    @Test
    @DisplayName("성공")
    void success() {
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      ReviewResponse result = reviewService.get(reviewId);

      assertNotNull(result);
      assertEquals(review.getTitle(), result.title());
      assertEquals(review.getComment(), result.comment());
      assertEquals(review.getRating(), result.rating());
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      UUID randomReviewId = UUID.randomUUID();

      when(reviewRepository.findById(randomReviewId)).thenReturn(Optional.empty());

      assertThrows(ReviewNotFoundException.class, () -> reviewService.get(randomReviewId));
    }
  }

  @Nested
  @DisplayName("유저별 리뷰 조회")
  class FindAllByUser {

    @Test
    @DisplayName("성공")
    void success() {
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

      List<ReviewResponse> result = reviewService.getAllByUser(userId);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(review.getTitle(), result.get(0).title());
      assertEquals(review2.getTitle(), result.get(1).title());
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      UUID randomUserId = UUID.randomUUID();

      when(userRepository.existsById(randomUserId)).thenReturn(false);

      assertThrows(UserNotFoundException.class, () -> reviewService.getAllByUser(randomUserId));
    }
  }

  @Nested
  @DisplayName("콘텐츠별 리뷰 조회")
  class FindAllByContent {

    @Test
    @DisplayName("성공")
    void success() {
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

      List<ReviewResponse> result = reviewService.getAllByContent(contentId);

      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(review.getTitle(), result.get(0).title());
      assertEquals(review2.getTitle(), result.get(1).title());
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      UUID randomContentId = UUID.randomUUID();

      when(contentRepository.existsById(randomContentId)).thenReturn(false);

      assertThrows(ContentNotFoundException.class, () -> reviewService.getAllByContent(randomContentId));
    }
  }

  @Nested
  @DisplayName("리뷰 삭제")
  class DeleteReview {

    @Test
    @DisplayName("성공")
    void success() {
      when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

      reviewService.delete(reviewId);

      verify(reviewRepository, times(1)).delete(review);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      UUID randomReviewId = UUID.randomUUID();

      when(reviewRepository.findById(randomReviewId)).thenReturn(Optional.empty());

      assertThrows(ReviewNotFoundException.class, () -> reviewService.delete(randomReviewId));

      verify(reviewRepository, never()).delete(any(Review.class));
    }
  }

  @Nested
  @DisplayName("유저별 리뷰 전체 삭제")
  class DeleteAllByUser {

    @Test
    @DisplayName("성공")
    void success() {
      when(userRepository.findById(userId)).thenReturn(Optional.of(user));

      reviewService.deleteAllByUser(userId);

      verify(reviewRepository, times(1)).deleteAllByUserId(userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      UUID randomUserId = UUID.randomUUID();

      when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());

      assertThrows(UserNotFoundException.class, () -> reviewService.deleteAllByUser(randomUserId));

      verify(reviewRepository, never()).deleteAllByUserId(any());
    }
  }
}