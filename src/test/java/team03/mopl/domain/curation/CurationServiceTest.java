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

import team03.mopl.common.exception.curation.KeywordAccessDeniedException;
import team03.mopl.common.exception.curation.KeywordDeleteDeniedException;
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.entity.ContentSearchResult;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.entity.KeywordContent;
import team03.mopl.domain.curation.repository.KeywordContentRepository;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.curation.service.CurationService;
import team03.mopl.domain.curation.service.ContentSearchService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("큐레이션 서비스 테스트")
class CurationServiceTest {

  @Mock
  private ContentSearchService contentSearchService;

  @Mock
  private KeywordRepository keywordRepository;

  @Mock
  private KeywordContentRepository keywordContentRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ContentRepository contentRepository;

  @InjectMocks
  private CurationService curationService;

  private User testUser;
  private Keyword testKeyword;
  private Content testContent;
  private Content testContent2;
  private UUID userId;
  private UUID keywordId;
  private UUID contentId;
  private UUID contentId2;

  @BeforeEach
  void setUp() {
    // 매 테스트마다 독립적인 UUID 생성
    userId = UUID.randomUUID();
    keywordId = UUID.randomUUID();
    contentId = UUID.randomUUID();
    contentId2 = UUID.randomUUID();

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

    testContent2 = Content.builder()
        .id(contentId2)
        .title("액션 드라마")
        .description("스릴러와 액션이 어우러진 드라마")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .avgRating(BigDecimal.valueOf(4.0))
        .build();

    // Mock 초기화
    reset(contentSearchService, keywordRepository, keywordContentRepository,
        userRepository, contentRepository);
  }

  @Nested
  @DisplayName("새 콘텐츠 배치 큐레이션 테스트")
  class BatchCurationForNewContentsTest {

    @Test
    @DisplayName("성공적인 새 콘텐츠 배치 큐레이션")
    void batchCurationForNewContents_Success() {
      // given
      List<Content> newContents = List.of(testContent, testContent2);
      List<Keyword> allKeywords = List.of(testKeyword);

      ContentSearchResult searchResult1 = new ContentSearchResult(testContent, 8.5);
      ContentSearchResult searchResult2 = new ContentSearchResult(testContent2, 7.0);
      List<ContentSearchResult> searchResults = List.of(searchResult1, searchResult2);

      when(keywordRepository.findAll()).thenReturn(allKeywords);
      when(contentSearchService.findContentsByKeywordWithScore("액션")).thenReturn(searchResults);
      when(keywordContentRepository.findContentIdsByKeyword(testKeyword)).thenReturn(List.of());

      // when
      assertDoesNotThrow(() -> {
        curationService.batchCurationForNewContents(newContents);
      });

      // then
      verify(contentSearchService, times(2)).indexContent(any(Content.class));
      verify(keywordRepository).findAll();
      verify(contentSearchService).findContentsByKeywordWithScore("액션");
      verify(keywordContentRepository).findContentIdsByKeyword(testKeyword);
      verify(keywordContentRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("빈 콘텐츠 목록으로 배치 큐레이션")
    void batchCurationForNewContents_EmptyContents() {
      // given
      List<Content> newContents = List.of();

      // when
      curationService.batchCurationForNewContents(newContents);

      // then
      verify(contentSearchService, never()).indexContent(any());
      verify(keywordRepository, never()).findAll();
    }

    @Test
    @DisplayName("null 콘텐츠 목록으로 배치 큐레이션")
    void batchCurationForNewContents_NullContents() {
      // when
      curationService.batchCurationForNewContents(null);

      // then
      verify(contentSearchService, never()).indexContent(any());
      verify(keywordRepository, never()).findAll();
    }

    @Test
    @DisplayName("기존 키워드가 없을 때 배치 큐레이션")
    void batchCurationForNewContents_NoKeywords() {
      // given
      List<Content> newContents = List.of(testContent);
      when(keywordRepository.findAll()).thenReturn(List.of());

      // when
      curationService.batchCurationForNewContents(newContents);

      // then
      verify(contentSearchService).indexContent(testContent);
      verify(keywordRepository).findAll();
      verify(contentSearchService, never()).findContentsByKeywordWithScore(any());
    }

    @Test
    @DisplayName("이미 매칭된 콘텐츠는 제외하고 새 매칭만 생성")
    void batchCurationForNewContents_ExistingMatches() {
      // given
      List<Content> newContents = List.of(testContent, testContent2);
      ContentSearchResult searchResult1 = new ContentSearchResult(testContent, 8.5);
      ContentSearchResult searchResult2 = new ContentSearchResult(testContent2, 7.0);

      when(keywordRepository.findAll()).thenReturn(List.of(testKeyword));
      when(contentSearchService.findContentsByKeywordWithScore("액션"))
          .thenReturn(List.of(searchResult1, searchResult2));
      when(keywordContentRepository.findContentIdsByKeyword(testKeyword))
          .thenReturn(List.of(contentId)); // testContent는 이미 매칭됨

      // when
      curationService.batchCurationForNewContents(newContents);

      // then
      verify(keywordContentRepository).saveAll(argThat(iterable -> {
        List<KeywordContent> list = (List<KeywordContent>) iterable;
        return list.size() == 1 &&
            list.get(0).getContent().getId().equals(contentId2);
      }));
    }

    @Test
    @DisplayName("콘텐츠 인덱싱 실패 시에도 배치 작업 계속")
    void batchCurationForNewContents_IndexingFailure() {
      // given
      List<Content> newContents = List.of(testContent);
      doThrow(new RuntimeException("Indexing failed")).when(contentSearchService).indexContent(testContent);
      when(keywordRepository.findAll()).thenReturn(List.of(testKeyword));

      // when
      curationService.batchCurationForNewContents(newContents);

      // then
      verify(contentSearchService).indexContent(testContent);
      verify(keywordRepository).findAll();
    }

    @Test
    @DisplayName("배치 큐레이션 중 예외 발생 시 RuntimeException 래핑")
    void batchCurationForNewContents_ExceptionWrapping() {
      // given
      List<Content> newContents = List.of(testContent);
      when(keywordRepository.findAll()).thenThrow(new RuntimeException("Database error"));

      // when & then
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        curationService.batchCurationForNewContents(newContents);
      });

      assertEquals("새 콘텐츠 큐레이션 배치 작업 실패", exception.getMessage());
    }
  }

  @Nested
  @DisplayName("키워드 등록 테스트")
  class RegisterKeywordTest {

    @Test
    @DisplayName("성공적인 키워드 등록")
    void registerKeyword_Success() {
      // given
      String keywordText = "액션";
      ContentSearchResult searchResult = new ContentSearchResult(testContent, 8.5);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(testKeyword);
      when(contentSearchService.findContentsByKeywordWithScore(keywordText))
          .thenReturn(List.of(searchResult));

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      assertEquals(keywordText, result.keyword());

      verify(userRepository).findById(userId);
      verify(keywordRepository).save(any(Keyword.class));
      verify(contentSearchService).findContentsByKeywordWithScore(keywordText);
      verify(keywordContentRepository).saveAll(anyList());
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
      verify(keywordRepository, never()).save(any());
    }

    @Test
    @DisplayName("관련 콘텐츠가 없는 키워드 등록")
    void registerKeyword_NoRelatedContent() {
      // given
      String keywordText = "존재하지않는키워드";
      Keyword savedKeyword = Keyword.builder()
          .id(keywordId)
          .user(testUser)
          .keyword(keywordText)
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(savedKeyword);
      when(contentSearchService.findContentsByKeywordWithScore(keywordText))
          .thenReturn(List.of());

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      assertEquals(keywordText, result.keyword());
      verify(contentSearchService).findContentsByKeywordWithScore(keywordText);
      verify(keywordContentRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("콘텐츠 매핑 실패 시에도 키워드 등록 성공")
    void registerKeyword_ContentMappingFailure() {
      // given
      String keywordText = "액션";
      Keyword savedKeyword = Keyword.builder()
          .id(keywordId)
          .user(testUser)
          .keyword(keywordText)
          .build();

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(savedKeyword);
      when(contentSearchService.findContentsByKeywordWithScore(keywordText))
          .thenThrow(new RuntimeException("Search failed"));

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      assertEquals(keywordText, result.keyword());
      verify(keywordRepository).save(any(Keyword.class));
    }
  }

  @Nested
  @DisplayName("키워드별 추천 조회 테스트")
  class GetRecommendationsByKeywordTest {

    @Test
    @DisplayName("성공적인 추천 조회")
    void getRecommendationsByKeyword_Success() {
      // given
      KeywordContent keywordContent1 = new KeywordContent(testKeyword, testContent, 0.9);
      KeywordContent keywordContent2 = new KeywordContent(testKeyword, testContent2, 0.8);
      List<KeywordContent> keywordContents = List.of(keywordContent1, keywordContent2);

      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.findByKeywordOrderByScoreDesc(testKeyword))
          .thenReturn(keywordContents);

      // when
      List<ContentDto> result = curationService.getRecommendationsByKeyword(keywordId, userId);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals(testContent.getTitle(), result.get(0).title());
      assertEquals(testContent2.getTitle(), result.get(1).title());

      verify(keywordRepository).findByIdAndUserId(keywordId, userId);
      verify(keywordContentRepository).findByKeywordOrderByScoreDesc(testKeyword);
    }

    @Test
    @DisplayName("키워드 접근 권한이 없을 때 예외 발생")
    void getRecommendationsByKeyword_AccessDenied() {
      // given
      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.empty());

      // when & then
      assertThrows(KeywordAccessDeniedException.class, () -> {
        curationService.getRecommendationsByKeyword(keywordId, userId);
      });

      verify(keywordRepository).findByIdAndUserId(keywordId, userId);
      verify(keywordContentRepository, never()).findByKeywordOrderByScoreDesc(any());
    }

    @Test
    @DisplayName("추천 콘텐츠가 없는 경우 빈 목록 반환")
    void getRecommendationsByKeyword_EmptyRecommendations() {
      // given
      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.of(testKeyword));
      when(keywordContentRepository.findByKeywordOrderByScoreDesc(testKeyword))
          .thenReturn(List.of());

      // when
      List<ContentDto> result = curationService.getRecommendationsByKeyword(keywordId, userId);

      // then
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("키워드 삭제 테스트")
  class DeleteKeywordTest {

    @Test
    @DisplayName("성공적인 키워드 삭제")
    void delete_Success() {
      // given
      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.of(testKeyword));

      // when
      assertDoesNotThrow(() -> {
        curationService.delete(keywordId, userId);
      });

      // then
      verify(keywordRepository).findByIdAndUserId(keywordId, userId);
      verify(keywordRepository).delete(testKeyword);
    }

    @Test
    @DisplayName("키워드 삭제 권한이 없을 때 예외 발생")
    void delete_DeleteDenied() {
      // given
      when(keywordRepository.findByIdAndUserId(keywordId, userId))
          .thenReturn(Optional.empty());

      // when & then
      assertThrows(KeywordDeleteDeniedException.class, () -> {
        curationService.delete(keywordId, userId);
      });

      verify(keywordRepository).findByIdAndUserId(keywordId, userId);
      verify(keywordRepository, never()).delete(any());
    }
  }

  @Nested
  @DisplayName("사용자별 키워드 조회 테스트")
  class GetKeywordsByUserTest {

    @Test
    @DisplayName("성공적인 사용자 키워드 조회")
    void getKeywordsByUser_Success() {
      // given
      Keyword keyword2 = Keyword.builder()
          .id(UUID.randomUUID())
          .user(testUser)
          .keyword("코미디")
          .build();

      when(keywordRepository.findByUserIdOrderByCreatedAtDesc(userId))
          .thenReturn(List.of(testKeyword, keyword2));

      // when
      List<KeywordDto> result = curationService.getKeywordsByUser(userId);

      // then
      assertNotNull(result);
      assertEquals(2, result.size());
      assertEquals("액션", result.get(0).keyword());
      assertEquals("코미디", result.get(1).keyword());

      verify(keywordRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("키워드가 없는 사용자의 빈 목록 반환")
    void getKeywordsByUser_EmptyList() {
      // given
      when(keywordRepository.findByUserIdOrderByCreatedAtDesc(userId))
          .thenReturn(List.of());

      // when
      List<KeywordDto> result = curationService.getKeywordsByUser(userId);

      // then
      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("키워드 조회 중 예외 발생 시 KeywordNotFoundException")
    void getKeywordsByUser_ExceptionThrown() {
      // given
      when(keywordRepository.findByUserIdOrderByCreatedAtDesc(userId))
          .thenThrow(new RuntimeException("Database error"));

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationService.getKeywordsByUser(userId);
      });
    }
  }

  @Nested
  @DisplayName("Elasticsearch 재색인 테스트")
  class ReindexAllContentsTest {

    @Test
    @DisplayName("성공적인 전체 재색인")
    void reindexAllContentsToElasticsearch_Success() {
      // given
      List<Content> allContents = List.of(testContent, testContent2);
      when(contentRepository.findAll()).thenReturn(allContents);

      // when
      assertDoesNotThrow(() -> {
        curationService.reindexAllContentsToElasticsearch();
      });

      // then
      verify(contentRepository).findAll();
      verify(contentSearchService, times(2)).indexContent(any(Content.class));
    }

    @Test
    @DisplayName("빈 콘텐츠 목록 재색인")
    void reindexAllContentsToElasticsearch_EmptyContents() {
      // given
      when(contentRepository.findAll()).thenReturn(List.of());

      // when
      curationService.reindexAllContentsToElasticsearch();

      // then
      verify(contentRepository).findAll();
      verify(contentSearchService, never()).indexContent(any());
    }

    @Test
    @DisplayName("개별 콘텐츠 인덱싱 실패 시에도 계속 진행")
    void reindexAllContentsToElasticsearch_PartialFailure() {
      // given
      List<Content> allContents = List.of(testContent, testContent2);
      when(contentRepository.findAll()).thenReturn(allContents);
      doThrow(new RuntimeException("Indexing failed")).when(contentSearchService).indexContent(testContent);

      // when
      curationService.reindexAllContentsToElasticsearch();

      // then
      verify(contentRepository).findAll();
      verify(contentSearchService, times(2)).indexContent(any(Content.class));
    }
  }

  @Nested
  @DisplayName("점수 계산 테스트")
  class ScoreCalculationTest {

    @Test
    @DisplayName("OpenSearch 점수와 평점을 조합한 최종 점수 계산 확인")
    void calculateFinalRelevanceScore_Integration() {
      // given
      String keywordText = "액션";

      // 높은 검색 점수와 높은 평점을 가진 콘텐츠
      Content highScoreContent = Content.builder()
          .id(UUID.randomUUID())
          .title("최고의 액션 영화")
          .description("액션이 최고인 영화")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(5.0))
          .build();

      ContentSearchResult highScoreResult = new ContentSearchResult(highScoreContent, 20.0);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(testKeyword);
      when(contentSearchService.findContentsByKeywordWithScore(keywordText))
          .thenReturn(List.of(highScoreResult));

      // when
      KeywordDto result = curationService.registerKeyword(userId, keywordText);

      // then
      assertNotNull(result);
      verify(keywordContentRepository).saveAll(argThat(iterable -> {
        List<KeywordContent> list = (List<KeywordContent>) iterable;
        if (list.isEmpty()) return false;
        KeywordContent kc = list.get(0);
        // 최종 점수는 0.8 (정규화된 검색 점수) + 0.2 (평점 보너스) = 1.0
        return kc.getScore() == 1.0;
      }));
    }

    @Test
    @DisplayName("낮은 검색 점수와 평점이 없는 콘텐츠의 점수 계산")
    void calculateFinalRelevanceScore_LowScoreNoRating() {
      // given
      String keywordText = "액션";

      Content lowScoreContent = Content.builder()
          .id(UUID.randomUUID())
          .title("액션 영화")
          .description("일반적인 영화")
          .contentType(ContentType.MOVIE)
          .avgRating(null) // 평점 없음
          .build();

      ContentSearchResult lowScoreResult = new ContentSearchResult(lowScoreContent, 5.0);

      when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
      when(keywordRepository.save(any(Keyword.class))).thenReturn(testKeyword);
      when(contentSearchService.findContentsByKeywordWithScore(keywordText))
          .thenReturn(List.of(lowScoreResult));

      // when
      curationService.registerKeyword(userId, keywordText);

      // then
      verify(keywordContentRepository).saveAll(argThat(iterable -> {
        List<KeywordContent> list = (List<KeywordContent>) iterable;
        if (list.isEmpty()) return false;
        KeywordContent kc = list.get(0);
        // 최종 점수는 0.25 (5.0/20.0) + 0.0 (평점 보너스 없음) = 0.25
        return kc.getScore() == 0.25;
      }));
    }
  }
}
