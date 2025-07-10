package team03.mopl.domain.review;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.review.ReviewNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.review.controller.ReviewController;
import team03.mopl.domain.review.dto.ReviewCreateRequest;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.dto.ReviewUpdateRequest;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 컨트롤러 테스트")
class ReviewControllerTest {

  @Mock
  private ReviewService reviewService;

  @Mock
  private CustomUserDetails userDetails;

  @InjectMocks
  private ReviewController reviewController;

  @Nested
  @DisplayName("리뷰 생성 요청")
  class CreateReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      UUID contentId = UUID.randomUUID();

      ReviewCreateRequest request = new ReviewCreateRequest(
          userId,
          contentId,
          "테스트 리뷰",
          "테스트 리뷰 내용",
          BigDecimal.valueOf(5)
      );

      ReviewDto mockResponse = new ReviewDto("테스트 리뷰", "테스트 리뷰 내용", BigDecimal.valueOf(5));

      when(reviewService.create(request)).thenReturn(mockResponse);

      // when
      ResponseEntity<ReviewDto> response = reviewController.create(request);

      // then
      assertNotNull(response.getBody());
      assertEquals(mockResponse.title(), response.getBody().title());
      verify(reviewService).create(request);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
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

      // when & then
      assertThrows(UserNotFoundException.class, () -> reviewController.create(request));
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      // given
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

      // when & then
      assertThrows(ContentNotFoundException.class, () -> reviewController.create(request));
    }
  }

  @Nested
  @DisplayName("리뷰 조회 요청")
  class GetReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID reviewId = UUID.randomUUID();

      ReviewDto mockResponse = new ReviewDto("테스트 리뷰", "테스트 리뷰 내용", BigDecimal.valueOf(5));

      when(reviewService.get(reviewId)).thenReturn(mockResponse);

      // when
      ResponseEntity<ReviewDto> response = reviewController.get(reviewId);

      // then
      assertNotNull(response.getBody());
      verify(reviewService).get(reviewId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      // given
      UUID reviewId = UUID.randomUUID();

      when(reviewService.get(reviewId)).thenThrow(new ReviewNotFoundException());

      // when & then
      assertThrows(ReviewNotFoundException.class, () -> reviewController.get(reviewId));
    }
  }

  @Nested
  @DisplayName("리뷰 수정 요청")
  class UpdateReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      ReviewDto mockResponse = new ReviewDto("테스트 리뷰", "테스트 리뷰 내용", BigDecimal.valueOf(5));

      when(userDetails.getId()).thenReturn(userId);
      when(reviewService.update(reviewId, request, userId)).thenReturn(mockResponse);

      // when
      ResponseEntity<ReviewDto> response = reviewController.update(reviewId, request, userDetails);

      // then
      assertNotNull(response.getBody());
      assertEquals("수정된 리뷰 제목", response.getBody().title());
      verify(reviewService).update(reviewId, request, userId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      when(userDetails.getId()).thenReturn(userId);
      when(reviewService.update(reviewId, request, userId)).thenThrow(new ReviewNotFoundException());

      // when & then
      assertThrows(ReviewNotFoundException.class, () -> reviewController.update(reviewId, request, userDetails));
    }

    @Test
    @DisplayName("다른 사용자의 리뷰 수정 시도")
    void failsWhenAccessDenied() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      ReviewUpdateRequest request = new ReviewUpdateRequest(
          "수정된 리뷰 제목",
          "수정된 리뷰 내용",
          BigDecimal.valueOf(4)
      );

      when(userDetails.getId()).thenReturn(userId);
      when(reviewService.update(reviewId, request, userId)).thenThrow(new AccessDeniedException("본인의 리뷰만 수정할 수 있습니다"));

      // when & then
      assertThrows(AccessDeniedException.class, () -> reviewController.update(reviewId, request, userDetails));
    }
  }

  @Nested
  @DisplayName("리뷰 삭제 요청")
  class DeleteReview {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);

      // when
      ResponseEntity<Void> response = reviewController.delete(reviewId, userDetails);

      // then
      assertEquals(204, response.getStatusCodeValue()); // NO_CONTENT
      assertNull(response.getBody());
      verify(reviewService).delete(reviewId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 리뷰")
    void failsWhenReviewNotFound() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new ReviewNotFoundException()).when(reviewService).delete(reviewId, userId);

      // when & then
      assertThrows(ReviewNotFoundException.class, () -> reviewController.delete(reviewId, userDetails));
    }

    @Test
    @DisplayName("다른 사용자의 리뷰 삭제 시도")
    void failsWhenAccessDenied() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new AccessDeniedException("본인의 리뷰만 삭제할 수 있습니다")).when(reviewService).delete(reviewId, userId);

      // when & then
      assertThrows(AccessDeniedException.class, () -> reviewController.delete(reviewId, userDetails));
    }

    @Test
    @DisplayName("사용자 인증 정보로부터 userId 추출")
    void extractsUserIdFromUserDetails() {
      // given
      UUID reviewId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);

      // when
      ResponseEntity<Void> response = reviewController.delete(reviewId, userDetails);

      // then
      verify(userDetails).getId();
      verify(reviewService).delete(reviewId, userId);
    }
  }
}