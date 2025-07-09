package team03.mopl.domain.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.review.ReviewNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.controller.ContentController;
import team03.mopl.domain.review.controller.ReviewController;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.domain.user.UserController;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 컨트롤러 테스트")
class ReviewControllerTest {

  @Mock
  private ReviewService reviewService;

  @InjectMocks
  private ReviewController reviewController;

  @InjectMocks
  private ContentController contentController;

  @InjectMocks
  private UserController userController;

  @Nested
  @DisplayName("리뷰 생성 요청")
  class CreateReview {

    @Test
    @DisplayName("성공")
    void success() {
      UUID userId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      ResponseEntity<ReviewResponse> reviewResponse = reviewController.create(request);

      verify(reviewService).create(request);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      UUID userId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(reviewService.create(request)).thenThrow(new UserNotFoundException());

      assertThrows(UserNotFoundException.class, () -> reviewController.create(request));
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      UUID userId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      when(reviewService.create(request)).thenThrow(new ContentNotFoundException());

      assertThrows(ContentNotFoundException.class, () -> reviewController.create(request));
    }
  }

  @Nested
  @DisplayName("리뷰 조회 요청")
  class FindReview {

    @Test
    @DisplayName("성공")
    void success() {
      UUID reviewId = UUID.randomUUID();

      ResponseEntity<ReviewResponse> response = reviewController.get(reviewId);

      verify(reviewService).get(reviewId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      UUID reviewId = UUID.randomUUID();

      when(reviewService.get(reviewId)).thenThrow(new ReviewNotFoundException());

      assertThrows(ReviewNotFoundException.class, () -> reviewController.get(reviewId));
    }
  }

  @Nested
  @DisplayName("유저별 리뷰 조회 요청")
  class FindAllByUser {

    @Test
    @DisplayName("성공")
    void success() {
      UUID userId = UUID.randomUUID();

      ResponseEntity<List<ReviewResponse>> response = userController.getAllByUser(userId);

      verify(reviewService).getAllByUser(userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      UUID userId = UUID.randomUUID();

      when(reviewService.getAllByUser(userId)).thenThrow(new UserNotFoundException());

      assertThrows(UserNotFoundException.class, () -> userController.getAllByUser(userId));
    }
  }

  @Nested
  @DisplayName("콘텐츠별 리뷰 조회 요청")
  class FindAllByContent {

    @Test
    @DisplayName("성공")
    void success() {
      UUID contentId = UUID.randomUUID();

      ResponseEntity<List<ReviewResponse>> response = contentController.getAllByContent(contentId);

      verify(reviewService).getAllByContent(contentId);
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      UUID contentId = UUID.randomUUID();

      when(reviewService.getAllByContent(contentId)).thenThrow(new ContentNotFoundException());

      assertThrows(ContentNotFoundException.class, () -> contentController.getAllByContent(contentId));
    }
  }

  @Nested
  @DisplayName("리뷰 수정 요청")
  class UpdateReview {

    @Test
    @DisplayName("성공")
    void success() {
      UUID reviewId = UUID.randomUUID();

      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      ResponseEntity<ReviewResponse> response = reviewController.update(reviewId, request);

      verify(reviewService).update(reviewId, request);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      UUID reviewId = UUID.randomUUID();

      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      when(reviewService.update(reviewId, request)).thenThrow(new ReviewNotFoundException());

      assertThrows(ReviewNotFoundException.class, () -> reviewController.update(reviewId, request));
    }
  }

  @Nested
  @DisplayName("리뷰 삭제 요청")
  class DeleteReview {

    @Test
    @DisplayName("성공")
    void success() {
      UUID reviewId = UUID.randomUUID();

      ResponseEntity<Void> response = reviewController.delete(reviewId);

      assertNull(response.getBody());

      verify(reviewService).delete(reviewId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      UUID reviewId = UUID.randomUUID();

      doThrow(new ReviewNotFoundException()).when(reviewService).delete(reviewId);

      assertThrows(ReviewNotFoundException.class, () -> reviewController.delete(reviewId));
    }
  }

  @Nested
  @DisplayName("유저별 리뷰 전체 삭제 요청")
  class DeleteAllByUser {

    @Test
    @DisplayName("성공")
    void success() {
      UUID userId = UUID.randomUUID();

      ResponseEntity<Void> response = userController.deleteAllByUser(userId);

      assertNull(response.getBody());

      verify(reviewService).deleteAllByUser(userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      UUID userId = UUID.randomUUID();

      doThrow(new UserNotFoundException()).when(reviewService).deleteAllByUser(userId);

      assertThrows(UserNotFoundException.class, () -> userController.deleteAllByUser(userId));
    }
  }
}