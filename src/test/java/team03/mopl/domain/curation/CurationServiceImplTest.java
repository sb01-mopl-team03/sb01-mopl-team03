package team03.mopl.domain.curation;

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
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.entity.KeywordContent;
import team03.mopl.domain.curation.repository.KeywordContentRepository;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.curation.service.CurationServiceImpl;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("큐레이션 서비스 테스트")
class CurationServiceImplTest {

  @Mock
  private ReviewService reviewService;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private KeywordRepository keywordRepository;

  @Mock
  private KeywordContentRepository keywordContentRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private CurationServiceImpl curationService;

  // 테스트용 유저
  private UUID userId;
  private User user;

  // 테스트용 콘텐츠
  private UUID contentId;
  private Content content;

  // 테스트용 키워드
  private UUID keywordId;
  private Keyword keyword;

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
        .title("테스트 액션 영화")
        .description("액션과 모험이 가득한 영화입니다.")
        .contentType(ContentType.MOVIE)
        .releaseDate(LocalDateTime.now())
        .avgRating(BigDecimal.valueOf(4.5))
        .build();

    keywordId = UUID.randomUUID();
    keyword = Keyword.builder()
        .id(keywordId)
        .user(user)
        .keyword("액션")
        .build();
  }

  @Nested
  @DisplayName("키워드 등록")
  class RegisterKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      String keywordText = "액션";

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
      when(contentRepository.findAll()).thenReturn(List.of(content));
      when(keywordContentRepository.save(any(KeywordContent.class))).thenReturn(new KeywordContent(keyword, content));

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      assertEquals("액션", result.keyword());
      assertEquals(userId, result.userId());

      verify(keywordRepository, times(1)).save(any(Keyword.class));
      verify(contentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();
      String keywordText = "액션";

      when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> curationService.registerKeyword(randomUserId, keywordText));

      verify(keywordRepository, never()).save(any(Keyword.class));
    }

    @Test
    @DisplayName("다국어 키워드 정규화")
    void normalizeMultilingualKeyword() {
      // given
      String keywordText = "Action Movie!!!";

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(keyword);
      when(contentRepository.findAll()).thenReturn(List.of());

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      verify(keywordRepository, times(1)).save(any(Keyword.class));
    }
  }

  @Nested
  @DisplayName("키워드별 콘텐츠 큐레이션")
  class CurateContentForKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      Content actionMovie = Content.builder()
          .id(UUID.randomUUID())
          .title("액션 블록버스터")
          .description("최고의 액션 영화")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(4.8))
          .build();

      Content dramaMovie = Content.builder()
          .id(UUID.randomUUID())
          .title("감동 드라마")
          .description("가족 이야기")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(4.0))
          .build();

      when(contentRepository.findAll()).thenReturn(List.of(actionMovie, dramaMovie));
      when(keywordContentRepository.save(any(KeywordContent.class))).thenReturn(new KeywordContent(keyword, actionMovie));

      // when
      List<ContentDto> result = curationService.curateContentForKeyword(keyword);

      // then
      assertNotNull(result);
      // 액션 관련 콘텐츠가 포함되어야 함
      assertTrue(result.stream().anyMatch(c -> c.title().contains("액션")));
    }

    @Test
    @DisplayName("매칭되는 콘텐츠가 없는 경우")
    void noMatchingContent() {
      // given
      Content unrelatedContent = Content.builder()
          .id(UUID.randomUUID())
          .title("로맨틱 코미디")
          .description("사랑 이야기")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(3.0))
          .build();

      when(contentRepository.findAll()).thenReturn(List.of(unrelatedContent));

      // when
      List<ContentDto> result = curationService.curateContentForKeyword(keyword);

      // then
      assertNotNull(result);
      // 임계값 이하의 콘텐츠는 포함되지 않을 수 있음
      verify(contentRepository, times(1)).findAll();
    }
  }

  @Nested
  @DisplayName("사용자 추천 콘텐츠 조회")
  class GetRecommendationsByKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      Keyword testKeyword = Keyword.builder()
          .id(keywordId)
          .user(user)
          .keyword("테스트키워드")
          .build();

      Content testContent = Content.builder()
          .id(UUID.randomUUID())
          .title("테스트 콘텐츠")
          .description("테스트 설명")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(4.5))
          .build();

      KeywordContent keywordContent = new KeywordContent(testKeyword, testContent);

      when(keywordRepository.findByIdAndUserId(keywordId, userId)).thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.findByKeywordId(keywordId)).thenReturn(List.of(keywordContent));

      // when
      List<ContentDto> result = curationService.getRecommendationsByKeyword(keywordId, userId);

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(testContent.getTitle(), result.get(0).title());

      verify(keywordRepository, times(1)).findByIdAndUserId(keywordId, userId);
      verify(keywordContentRepository, times(1)).findByKeywordId(keywordId);
    }

    @Test
    @DisplayName("키워드를 찾을 수 없는 경우 예외 발생")
    void keywordNotFound() {
      // given
      when(keywordRepository.findByIdAndUserId(keywordId, userId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationService.getRecommendationsByKeyword(keywordId, userId);
      });

      verify(keywordRepository, times(1)).findByIdAndUserId(keywordId, userId);
      verify(keywordContentRepository, never()).findByKeywordId(any());
    }

    @Test
    @DisplayName("키워드는 존재하지만 매칭된 콘텐츠가 없는 경우")
    void noMatchingContents() {
      // given
      Keyword testKeyword = Keyword.builder()
          .id(keywordId)
          .user(user)
          .keyword("테스트키워드")
          .build();

      when(keywordRepository.findByIdAndUserId(keywordId, userId)).thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.findByKeywordId(keywordId)).thenReturn(List.of()); // 빈 리스트

      // when
      List<ContentDto> result = curationService.getRecommendationsByKeyword(keywordId, userId);

      // then
      assertNotNull(result);
      assertTrue(result.isEmpty());

      verify(keywordRepository, times(1)).findByIdAndUserId(keywordId, userId);
      verify(keywordContentRepository, times(1)).findByKeywordId(keywordId);
    }

    @Test
    @DisplayName("다른 사용자의 키워드에 접근하는 경우 예외 발생")
    void accessOtherUserKeyword() {
      // given
      UUID otherUserId = UUID.randomUUID();
      when(keywordRepository.findByIdAndUserId(keywordId, otherUserId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationService.getRecommendationsByKeyword(keywordId, otherUserId);
      });

      verify(keywordRepository, times(1)).findByIdAndUserId(keywordId, otherUserId);
    }
  }

  @Nested
  @DisplayName("신규 콘텐츠 배치 큐레이션")
  class BatchCurationForNewContents {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      List<Content> newContents = List.of(content);

      when(keywordRepository.findAll()).thenReturn(List.of(keyword));
      when(keywordContentRepository.existsByKeywordIdAndContentId(any(), any())).thenReturn(false);
      when(keywordContentRepository.save(any(KeywordContent.class))).thenReturn(new KeywordContent(keyword, content));

      // when
      curationService.batchCurationForNewContents(newContents);

      // then
      verify(keywordRepository, times(1)).findAll();
      // 점수가 임계값(0.25)을 넘으면 저장되어야 함
    }

    @Test
    @DisplayName("이미 존재하는 키워드-콘텐츠 관계")
    void alreadyExistingRelation() {
      // given
      List<Content> newContents = List.of(content);

      when(keywordRepository.findAll()).thenReturn(List.of(keyword));
      when(keywordContentRepository.existsByKeywordIdAndContentId(keywordId, contentId)).thenReturn(true);

      // when
      curationService.batchCurationForNewContents(newContents);

      // then
      verify(keywordRepository, times(1)).findAll();
      verify(keywordContentRepository, never()).save(any(KeywordContent.class));
    }

    @Test
    @DisplayName("빈 콘텐츠 리스트")
    void emptyContentList() {
      // given
      List<Content> newContents = List.of();

      when(keywordRepository.findAll()).thenReturn(List.of(keyword));

      // when
      curationService.batchCurationForNewContents(newContents);

      // then
      verify(keywordRepository, times(1)).findAll();
      verify(keywordContentRepository, never()).save(any(KeywordContent.class));
    }
  }

  @Nested
  @DisplayName("콘텐츠 평점 업데이트")
  class UpdateContentRating {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID reviewId1 = UUID.randomUUID();
      UUID reviewId2 = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      String authorName = "테스트사용자";

      ReviewDto review1 = new ReviewDto(
          reviewId1,
          authorId,
          authorName,
          "좋은 영화",
          "재미있어요",
          LocalDateTime.now(),
          BigDecimal.valueOf(5)
      );

      ReviewDto review2 = new ReviewDto(
          reviewId2,
          authorId,
          authorName,
          "괜찮은 영화",
          "보통이에요",
          LocalDateTime.now(),
          BigDecimal.valueOf(3)
      );

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(reviewService.getAllByContent(contentId)).thenReturn(List.of(review1, review2));
      when(contentRepository.save(any(Content.class))).thenReturn(content);

      // when
      curationService.updateContentRating(contentId);

      // then
      verify(contentRepository, times(1)).findById(contentId);
      verify(reviewService, times(1)).getAllByContent(contentId);
      verify(contentRepository, times(1)).save(any(Content.class));
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠")
    void failsWhenContentNotFound() {
      // given
      UUID randomContentId = UUID.randomUUID();

      when(contentRepository.findById(randomContentId)).thenReturn(Optional.empty());

      // when
      curationService.updateContentRating(randomContentId);

      // then
      verify(contentRepository, times(1)).findById(randomContentId);
      verify(contentRepository, never()).save(any(Content.class));
      verify(reviewService, never()).getAllByContent(any());
    }

    @Test
    @DisplayName("리뷰가 없는 경우")
    void noReviews() {
      // given
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(reviewService.getAllByContent(contentId)).thenReturn(List.of());
      when(contentRepository.save(any(Content.class))).thenReturn(content);

      // when
      curationService.updateContentRating(contentId);

      // then
      verify(contentRepository, times(1)).findById(contentId);
      verify(reviewService, times(1)).getAllByContent(contentId);
      verify(contentRepository, times(1)).save(any(Content.class));
    }

    @Test
    @DisplayName("평점이 null인 리뷰가 포함된 경우")
    void reviewsWithNullRating() {
      // given
      UUID reviewId1 = UUID.randomUUID();
      UUID reviewId2 = UUID.randomUUID();
      UUID authorId = UUID.randomUUID();
      String authorName = "테스트사용자";

      ReviewDto reviewWithNullRating = new ReviewDto(
          reviewId1,
          authorId,
          authorName,
          "평점 없는 리뷰",
          "평점을 주지 않았어요",
          LocalDateTime.now(),
          null
      );

      ReviewDto normalReview = new ReviewDto(
          reviewId2,
          authorId,
          authorName,
          "일반 리뷰",
          "좋아요",
          LocalDateTime.now(),
          BigDecimal.valueOf(4)
      );

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(reviewService.getAllByContent(contentId)).thenReturn(List.of(reviewWithNullRating, normalReview));
      when(contentRepository.save(any(Content.class))).thenReturn(content);

      // when
      curationService.updateContentRating(contentId);

      // then
      verify(contentRepository, times(1)).findById(contentId);
      verify(reviewService, times(1)).getAllByContent(contentId);
      verify(contentRepository, times(1)).save(any(Content.class));
    }
  }

  @Nested
  @DisplayName("키워드 삭제")
  class DeleteKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      when(keywordRepository.findById(keywordId)).thenReturn(Optional.of(keyword));

      // when
      curationService.delete(keywordId, userId);

      // then
      verify(keywordRepository, times(1)).findById(keywordId);
      verify(keywordRepository, times(1)).delete(keyword);
    }

    @Test
    @DisplayName("키워드를 찾을 수 없는 경우")
    void keywordNotFound() {
      // given
      when(keywordRepository.findById(keywordId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationService.delete(keywordId, userId);
      });

      verify(keywordRepository, times(1)).findById(keywordId);
      verify(keywordRepository, never()).delete(any());
    }

    @Test
    @DisplayName("다른 사용자의 키워드 삭제 시도")
    void deleteOtherUserKeyword() {
      // given
      UUID otherUserId = UUID.randomUUID();
      User otherUser = User.builder()
          .id(otherUserId)
          .name("다른유저")
          .email("other@test.com")
          .password("test")
          .role(Role.USER)
          .build();

      Keyword otherUserKeyword = Keyword.builder()
          .id(keywordId)
          .user(otherUser)
          .keyword("액션")
          .build();

      when(keywordRepository.findById(keywordId)).thenReturn(Optional.of(otherUserKeyword));

      // when & then
      assertThrows(team03.mopl.common.exception.curation.KeywordDeleteDeniedException.class, () -> {
        curationService.delete(keywordId, userId);
      });

      verify(keywordRepository, times(1)).findById(keywordId);
      verify(keywordRepository, never()).delete(any());
    }
  }
}