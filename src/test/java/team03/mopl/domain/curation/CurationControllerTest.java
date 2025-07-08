package team03.mopl.domain.curation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.curation.controller.CurationController;
import team03.mopl.domain.curation.dto.KeywordRequest;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.service.CurationService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;

@ExtendWith(MockitoExtension.class)
@DisplayName("큐레이션 컨트롤러 테스트")
class CurationControllerTest {

  @Mock
  private CurationService curationService;

  @InjectMocks
  private CurationController curationController;

  @Nested
  @DisplayName("키워드 등록 요청")
  class RegisterKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      String keywordText = "액션";

      KeywordRequest request = new KeywordRequest(userId, keywordText);

      User mockUser = User.builder()
          .id(userId)
          .name("테스트유저")
          .email("test@test.com")
          .password("test")
          .role(Role.USER)
          .build();

      Keyword mockKeyword = Keyword.builder()
          .user(mockUser)
          .keyword(keywordText)
          .build();

      when(curationService.registerKeyword(userId, keywordText)).thenReturn(mockKeyword);

      // when
      ResponseEntity<Keyword> response = curationController.registerKeyword(request);

      // then
      assertNotNull(response.getBody());
      assertEquals(keywordText, response.getBody().getKeyword());
      assertEquals(mockUser, response.getBody().getUser());

      verify(curationService).registerKeyword(userId, keywordText);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      String keywordText = "액션";

      KeywordRequest request = new KeywordRequest(userId, keywordText);

      when(curationService.registerKeyword(userId, keywordText)).thenThrow(new UserNotFoundException());

      // when & then
      assertThrows(UserNotFoundException.class, () -> curationController.registerKeyword(request));
    }
  }

  @Nested
  @DisplayName("사용자 추천 콘텐츠 조회 요청")
  class GetRecommendations {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      int limit = 10;

      Content mockContent1 = Content.builder()
          .id(UUID.randomUUID())
          .title("액션 영화")
          .description("액션과 모험이 가득한 영화")
          .contentType(ContentType.MOVIE)
          .releaseDate(LocalDateTime.now())
          .avgRating(BigDecimal.valueOf(4.5))
          .build();

      Content mockContent2 = Content.builder()
          .id(UUID.randomUUID())
          .title("스릴러 드라마")
          .description("긴장감 넘치는 드라마")
          .contentType(ContentType.TV)
          .releaseDate(LocalDateTime.now())
          .avgRating(BigDecimal.valueOf(4.0))
          .build();

      List<Content> mockRecommendations = List.of(mockContent1, mockContent2);

      when(curationService.getRecommendationsForUser(userId, limit)).thenReturn(mockRecommendations);

      // when
      ResponseEntity<List<Content>> response = curationController.getRecommendations(userId, limit);

      // then
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().size());
      assertEquals(mockContent1.getTitle(), response.getBody().get(0).getTitle());
      assertEquals(mockContent2.getTitle(), response.getBody().get(1).getTitle());

      verify(curationService).getRecommendationsForUser(userId, limit);
    }

    @Test
    @DisplayName("기본 limit 값 사용")
    void successWithDefaultLimit() {
      // given
      UUID userId = UUID.randomUUID();
      int defaultLimit = 10;

      List<Content> mockRecommendations = List.of();

      when(curationService.getRecommendationsForUser(userId, defaultLimit)).thenReturn(mockRecommendations);

      // when
      ResponseEntity<List<Content>> response = curationController.getRecommendations(userId, defaultLimit);

      // then
      assertNotNull(response.getBody());
      assertTrue(response.getBody().isEmpty());

      verify(curationService).getRecommendationsForUser(userId, defaultLimit);
    }

    @Test
    @DisplayName("빈 추천 목록 반환")
    void returnsEmptyRecommendations() {
      // given
      UUID userId = UUID.randomUUID();
      int limit = 5;

      List<Content> emptyRecommendations = List.of();

      when(curationService.getRecommendationsForUser(userId, limit)).thenReturn(emptyRecommendations);

      // when
      ResponseEntity<List<Content>> response = curationController.getRecommendations(userId, limit);

      // then
      assertNotNull(response.getBody());
      assertTrue(response.getBody().isEmpty());

      verify(curationService).getRecommendationsForUser(userId, limit);
    }

    @Test
    @DisplayName("limit 값보다 적은 추천 콘텐츠")
    void returnsFewerThanLimit() {
      // given
      UUID userId = UUID.randomUUID();
      int limit = 10;

      Content mockContent = Content.builder()
          .id(UUID.randomUUID())
          .title("유일한 추천 콘텐츠")
          .description("추천된 유일한 콘텐츠")
          .contentType(ContentType.MOVIE)
          .releaseDate(LocalDateTime.now())
          .avgRating(BigDecimal.valueOf(3.5))
          .build();

      List<Content> mockRecommendations = List.of(mockContent);

      when(curationService.getRecommendationsForUser(userId, limit)).thenReturn(mockRecommendations);

      // when
      ResponseEntity<List<Content>> response = curationController.getRecommendations(userId, limit);

      // then
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
      assertTrue(response.getBody().size() < limit);
      assertEquals(mockContent.getTitle(), response.getBody().get(0).getTitle());

      verify(curationService).getRecommendationsForUser(userId, limit);
    }
  }
}