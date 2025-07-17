package team03.mopl.domain.curation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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

import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.InvalidCursorFormatException;
import team03.mopl.common.exception.InvalidPageSizeException;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.curation.ContentRatingUpdateException;
import team03.mopl.common.exception.curation.KeywordDeleteDeniedException;
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.dto.CursorPageRequest;
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

  private User testUser;
  private Keyword testKeyword;
  private Content testContent;
  private UUID userId;
  private UUID keywordId;
  private UUID contentId;

  @BeforeEach
  void setUp() {
    // 매 테스트마다 독립적인 UUID 생성
    userId = UUID.randomUUID();
    keywordId = UUID.randomUUID();
    contentId = UUID.randomUUID();

    // 테스트 데이터 초기화
    testUser = User.builder()
        .id(userId)
        .name("테스트유저")
        .email("test@test.com")
        .password("password")
        .role(Role.USER)
        .build();

    testKeyword = Keyword.builder()
        .id(keywordId)
        .user(testUser)
        .keyword("액션")
        .build();

    testContent = Content.builder()
        .id(contentId)
        .title("액션 영화")
        .description("액션과 모험이 가득한 영화")
        .contentType(ContentType.MOVIE)
        .releaseDate(LocalDateTime.now())
        .avgRating(BigDecimal.valueOf(4.5))
        .build();

    // Mock 초기화
    reset(reviewService, contentRepository, keywordRepository, keywordContentRepository,
        userRepository);
  }

  @Nested
  @DisplayName("키워드 등록 테스트")
  class RegisterKeywordTest {

    @Test
    @DisplayName("성공적인 키워드 등록")
    void registerKeyword_Success() {
      // given
      String keywordText = "액션";
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(testKeyword);

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      assertEquals(userId, result.userId());
      assertEquals("액션", result.keyword());

      verify(userRepository).findById(userId);
      verify(keywordRepository).save(any(Keyword.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 키워드 등록 시 예외 발생")
    void registerKeyword_UserNotFound() {
      // given
      String keywordText = "액션";
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class, () -> {
        curationService.registerKeyword(userId, keywordText);
      });

      verify(userRepository).findById(userId);
      verify(keywordRepository, never()).save(any(Keyword.class));
    }

    @Test
    @DisplayName("공백 키워드 정규화")
    void registerKeyword_WithWhitespace() {
      // given
      String keywordText = "  액션  ";
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(testKeyword);

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      assertEquals("액션", result.keyword());

      verify(userRepository).findById(userId);
      verify(keywordRepository).save(any(Keyword.class));
    }
  }

  @Nested
  @DisplayName("키워드별 추천 조회 테스트")
  class GetRecommendationsByKeywordTest {

    @Test
    @DisplayName("성공적인 추천 조회")
    void getRecommendations_Success() {
      // given
      CursorPageRequest request = new CursorPageRequest(null, 10);

      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.existsByKeywordId(keywordId))
          .thenReturn(true);

      KeywordContent keywordContent = KeywordContent.builder()
          .keyword(testKeyword)
          .content(testContent)
          .score(0.8)
          .build();

      when(keywordContentRepository.findByKeywordIdWithPagination(
          eq(keywordId), isNull(), isNull(), eq(11)))
          .thenReturn(List.of(keywordContent));
      when(keywordContentRepository.countByKeywordId(keywordId))
          .thenReturn(1L);

      // when
      CursorPageResponseDto<ContentDto> result = curationService
          .getRecommendationsByKeyword(keywordId, userId, request);

      // then
      assertNotNull(result);
      assertEquals(1, result.data().size());
      assertEquals(1L, result.totalElements());
      assertFalse(result.hasNext());
      assertNull(result.nextCursor());

      ContentDto contentDto = result.data().get(0);
      assertEquals(testContent.getId(), contentDto.id());
      assertEquals(testContent.getTitle(), contentDto.title());
    }

    @Test
    @DisplayName("키워드가 존재하지 않을 때 예외 발생")
    void getRecommendations_KeywordNotFound() {
      // given
      CursorPageRequest request = new CursorPageRequest(null, 10);
      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.empty());

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationService.getRecommendationsByKeyword(keywordId, userId, request);
      });

      verify(keywordRepository).findByIdAndUserId(keywordId, userId);
    }

    @Test
    @DisplayName("잘못된 페이지 크기로 요청 시 예외 발생")
    void getRecommendations_InvalidPageSize() {
      // given
      CursorPageRequest request = new CursorPageRequest(null, 0);

      // when & then
      assertThrows(InvalidPageSizeException.class, () -> {
        curationService.getRecommendationsByKeyword(keywordId, userId, request);
      });
    }

    @Test
    @DisplayName("페이지 크기가 너무 클 때 예외 발생")
    void getRecommendations_PageSizeTooLarge() {
      // given
      CursorPageRequest request = new CursorPageRequest(null, 100);

      // when & then
      assertThrows(InvalidPageSizeException.class, () -> {
        curationService.getRecommendationsByKeyword(keywordId, userId, request);
      });
    }

    @Test
    @DisplayName("점수가 계산되지 않은 경우 빈 결과 반환")
    void getRecommendations_NoScoresCalculated() {
      // given
      CursorPageRequest request = new CursorPageRequest(null, 10);

      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.existsByKeywordId(keywordId))
          .thenReturn(false);

      // when
      CursorPageResponseDto<ContentDto> result = curationService
          .getRecommendationsByKeyword(keywordId, userId, request);

      // then
      assertNotNull(result);
      assertTrue(result.data().isEmpty());
      assertEquals(0, result.size());
      assertEquals(0L, result.totalElements());
      assertFalse(result.hasNext());
      assertNull(result.nextCursor());
    }

    @Test
    @DisplayName("잘못된 커서 형식으로 요청 시 예외 발생")
    void getRecommendations_InvalidCursor() {
      // given
      CursorPageRequest request = new CursorPageRequest("invalid-cursor", 10);

      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.existsByKeywordId(keywordId))
          .thenReturn(true);

      // when & then
      assertThrows(InvalidCursorFormatException.class, () -> {
        curationService.getRecommendationsByKeyword(keywordId, userId, request);
      });
    }

    @Test
    @DisplayName("페이지네이션 with hasNext")
    void getRecommendations_WithPagination() {
      // given
      CursorPageRequest request = new CursorPageRequest(null, 1);

      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.existsByKeywordId(keywordId))
          .thenReturn(true);

      Content content2 = Content.builder()
          .id(UUID.randomUUID())
          .title("액션 영화 2")
          .description("두 번째 액션 영화")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(4.0))
          .build();

      KeywordContent keywordContent1 = KeywordContent.builder()
          .keyword(testKeyword)
          .content(testContent)
          .score(0.9)
          .build();

      KeywordContent keywordContent2 = KeywordContent.builder()
          .keyword(testKeyword)
          .content(content2)
          .score(0.8)
          .build();

      when(keywordContentRepository.findByKeywordIdWithPagination(
          eq(keywordId), isNull(), isNull(), eq(2)))
          .thenReturn(List.of(keywordContent1, keywordContent2));
      when(keywordContentRepository.countByKeywordId(keywordId))
          .thenReturn(2L);

      // when
      CursorPageResponseDto<ContentDto> result = curationService
          .getRecommendationsByKeyword(keywordId, userId, request);

      // then
      assertNotNull(result);
      assertEquals(1, result.data().size());
      assertEquals(2L, result.totalElements());
      assertTrue(result.hasNext());
      assertNotNull(result.nextCursor());
    }
  }

  @Nested
  @DisplayName("사용자별 키워드 조회 테스트")
  class GetKeywordsByUserTest {

    @Test
    @DisplayName("성공적인 사용자 키워드 조회")
    void getKeywordsByUser_Success() {
      // given
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.findAllByUserId(userId)).thenReturn(List.of(testKeyword));

      // when
      List<KeywordDto> result = curationService.getKeywordsByUser(userId);

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(testKeyword.getKeyword(), result.get(0).keyword());

      verify(userRepository).findById(userId);
      verify(keywordRepository).findAllByUserId(userId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 키워드 조회 시 예외 발생")
    void getKeywordsByUser_UserNotFound() {
      // given
      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class, () -> {
        curationService.getKeywordsByUser(userId);
      });

      verify(userRepository).findById(userId);
      verify(keywordRepository, never()).findAllByUserId(any());
    }

    @Test
    @DisplayName("키워드가 없는 사용자의 빈 목록 반환")
    void getKeywordsByUser_EmptyList() {
      // given
      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.findAllByUserId(userId)).thenReturn(List.of());

      // when
      List<KeywordDto> result = curationService.getKeywordsByUser(userId);

      // then
      assertNotNull(result);
      assertTrue(result.isEmpty());

      verify(userRepository).findById(userId);
      verify(keywordRepository).findAllByUserId(userId);
    }
  }

  @Nested
  @DisplayName("키워드 삭제 테스트")
  class DeleteKeywordTest {

    @Test
    @DisplayName("성공적인 키워드 삭제")
    void deleteKeyword_Success() {
      // given
      when(keywordRepository.findById(keywordId)).thenReturn(Optional.of(testKeyword));

      // when
      assertDoesNotThrow(() -> {
        curationService.delete(keywordId, userId);
      });

      // then
      verify(keywordRepository).findById(keywordId);
      verify(keywordRepository).delete(testKeyword);
    }

    @Test
    @DisplayName("존재하지 않는 키워드 삭제 시 예외 발생")
    void deleteKeyword_KeywordNotFound() {
      // given
      when(keywordRepository.findById(keywordId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationService.delete(keywordId, userId);
      });

      verify(keywordRepository).findById(keywordId);
      verify(keywordRepository, never()).delete(any());
    }

    @Test
    @DisplayName("다른 사용자의 키워드 삭제 시 예외 발생")
    void deleteKeyword_UnauthorizedUser() {
      // given
      UUID otherUserId = UUID.randomUUID();
      when(keywordRepository.findById(keywordId)).thenReturn(Optional.of(testKeyword));

      // when & then
      assertThrows(KeywordDeleteDeniedException.class, () -> {
        curationService.delete(keywordId, otherUserId);
      });

      verify(keywordRepository).findById(keywordId);
      verify(keywordRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("콘텐츠 평점 업데이트 테스트")
  class UpdateContentRatingTest {

    @DisplayName("성공적인 콘텐츠 평점 업데이트")
    @Test
    void updateContentRating_Success() {

      // given
      UUID contentId = UUID.randomUUID();
      Content content = Content.builder()
          .id(contentId)
          .title("액션 영화")
          .description("액션 영화입니다")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(3.0))
          .build();

      List<ReviewDto> reviews = List.of(
          new ReviewDto(UUID.randomUUID(), UUID.randomUUID(), "사용자1", "좋은 영화", null,
              LocalDateTime.now(), BigDecimal.valueOf(4.0)),
          new ReviewDto(UUID.randomUUID(), UUID.randomUUID(), "사용자2",
              "최고의 영화", null, LocalDateTime.now(), BigDecimal.valueOf(5.0))
      );

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));
      when(reviewService.getAllByContent(contentId)).thenReturn(reviews);

      // when
      curationService.updateContentRating(contentId);

      // then
      verify(contentRepository, times(1)).findById(contentId);
      verify(reviewService, times(1)).getAllByContent(contentId);
      verify(contentRepository, times(1)).save(content);

      // 평점이 올바르게 계산되었는지 확인 (4.0 + 5.0) / 2 = 4.5
      assertEquals(BigDecimal.valueOf(4.50).setScale(2), content.getAvgRating());
    }

    @Test
    @DisplayName("존재하지 않는 콘텐츠 평점 업데이트 시 예외 발생")
    void updateContentRating_ContentNotFound() {
      // given
      when(contentRepository.findById(contentId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(ContentNotFoundException.class, () -> {
        curationService.updateContentRating(contentId);
      });

      verify(contentRepository).findById(contentId);
      verify(reviewService, never()).getAllByContent(any());
    }

    @Test
    @DisplayName("리뷰가 없는 콘텐츠의 평점 업데이트")
    void updateContentRating_NoReviews() {
      // given
      when(contentRepository.findById(contentId)).thenReturn(Optional.of(testContent));
      when(reviewService.getAllByContent(contentId)).thenReturn(List.of());
      when(contentRepository.save(any(Content.class))).thenReturn(testContent);

      // when
      assertDoesNotThrow(() -> {
        curationService.updateContentRating(contentId);
      });

      // then
      verify(contentRepository).findById(contentId);
      verify(reviewService).getAllByContent(contentId);
      verify(contentRepository).save(any(Content.class));
    }

    @Test
    @DisplayName("리뷰 서비스 예외 발생 시 ContentRatingUpdateException 발생")
    void updateContentRating_ReviewServiceException() {
      // given
      UUID contentId = UUID.randomUUID();
      Content content = Content.builder()
          .id(contentId)
          .title("액션 영화")
          .description("액션 영화입니다")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(4.5))
          .build();

      when(contentRepository.findById(contentId)).thenReturn(Optional.of(content));

      // reviewService.getAllByContent()에서 예외 발생 시키기
      when(reviewService.getAllByContent(contentId))
          .thenThrow(new RuntimeException("Review service error"));

      // when & then
      ContentRatingUpdateException exception = assertThrows(ContentRatingUpdateException.class,
          () -> {
            curationService.updateContentRating(contentId);
          });

      verify(contentRepository, times(1)).findById(contentId);
      verify(reviewService, times(1)).getAllByContent(contentId);
      // save는 호출되지 않아야 함 (예외 발생으로 인해)
      verify(contentRepository, never()).save(any(Content.class));
    }

    @Nested
    @DisplayName("신규 콘텐츠 배치 큐레이션 테스트")
    class BatchCurationTest {

      @Test
      @DisplayName("성공적인 신규 콘텐츠 배치 큐레이션")
      void batchCurationForNewContents_Success() {
        // given
        List<Content> newContents = List.of(testContent);
        when(keywordRepository.findAll()).thenReturn(List.of(testKeyword));
        when(keywordContentRepository.existsByKeywordIdAndContentId(keywordId, contentId))
            .thenReturn(false);
        when(keywordContentRepository.save(any(KeywordContent.class)))
            .thenReturn(mock(KeywordContent.class));

        // when
        assertDoesNotThrow(() -> {
          curationService.batchCurationForNewContents(newContents);
        });

        // then
        verify(keywordRepository).findAll();
        verify(keywordContentRepository).existsByKeywordIdAndContentId(keywordId, contentId);
        verify(keywordContentRepository).save(any(KeywordContent.class));
      }

      @Test
      @DisplayName("빈 콘텐츠 목록으로 배치 큐레이션")
      void batchCurationForNewContents_EmptyContents() {
        // given
        List<Content> newContents = List.of();

        // when
        assertDoesNotThrow(() -> {
          curationService.batchCurationForNewContents(newContents);
        });

        // then
        verify(keywordRepository, never()).findAll();
        verify(keywordContentRepository, never()).save(any());
      }

      @Test
      @DisplayName("키워드가 없을 때 배치 큐레이션")
      void batchCurationForNewContents_NoKeywords() {
        // given
        List<Content> newContents = List.of(testContent);
        when(keywordRepository.findAll()).thenReturn(List.of());

        // when
        assertDoesNotThrow(() -> {
          curationService.batchCurationForNewContents(newContents);
        });

        // then
        verify(keywordRepository).findAll();
        verify(keywordContentRepository, never()).save(any());
      }

      @Test
      @DisplayName("이미 존재하는 키워드-콘텐츠 매칭 건너뛰기")
      void batchCurationForNewContents_ExistingMatch() {
        // given
        List<Content> newContents = List.of(testContent);
        when(keywordRepository.findAll()).thenReturn(List.of(testKeyword));
        when(keywordContentRepository.existsByKeywordIdAndContentId(keywordId, contentId))
            .thenReturn(true);

        // when
        assertDoesNotThrow(() -> {
          curationService.batchCurationForNewContents(newContents);
        });

        // then
        verify(keywordRepository).findAll();
        verify(keywordContentRepository).existsByKeywordIdAndContentId(keywordId, contentId);
        verify(keywordContentRepository, never()).save(any());
      }
    }

    @Nested
    @DisplayName("초기화 테스트")
    class InitializationTest {

      @Test
      @DisplayName("서비스 초기화 성공")
      void init_Success() {
        // when & then
        assertDoesNotThrow(() -> {
          curationService.init();
        });
      }
    }
  }
}
