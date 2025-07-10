package team03.mopl.domain.curation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
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
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.curation.controller.CurationController;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.dto.KeywordRequest;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.repository.KeywordRepository;
import team03.mopl.domain.curation.service.CurationService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("큐레이션 컨트롤러 테스트")
class CurationControllerTest {

  @Mock
  private CurationService curationService;

  @Mock
  private KeywordRepository keywordRepository;

  @Mock
  private CustomUserDetails userDetails;

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

      when(curationService.registerKeyword(userId, keywordText)).thenReturn(
          KeywordDto.from(mockKeyword));

      // when
      ResponseEntity<KeywordDto> response = curationController.registerKeyword(request);

      // then
      assertNotNull(response.getBody());
      assertEquals(keywordText, response.getBody().keyword());
      assertEquals(mockUser.getId(), response.getBody().userId());

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
  @DisplayName("키워드별 콘텐츠 조회 요청")
  class GetRecommendations {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();

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

      when(userDetails.getUsername()).thenReturn(userId.toString());
      when(curationService.getRecommendationsByKeyword(keywordId, userId)).thenReturn(mockRecommendations);

      // when
      ResponseEntity<List<Content>> response = curationController.getRecommendations(keywordId, userDetails);

      // then
      assertNotNull(response.getBody());
      assertEquals(2, response.getBody().size());
      assertEquals(mockContent1.getTitle(), response.getBody().get(0).getTitle());
      assertEquals(mockContent2.getTitle(), response.getBody().get(1).getTitle());

      verify(curationService).getRecommendationsByKeyword(keywordId, userId);
    }

    @Test
    @DisplayName("빈 추천 목록 반환")
    void returnsEmptyRecommendations() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();

      List<Content> emptyRecommendations = List.of();

      when(userDetails.getUsername()).thenReturn(userId.toString());
      when(curationService.getRecommendationsByKeyword(keywordId, userId)).thenReturn(emptyRecommendations);

      // when
      ResponseEntity<List<Content>> response = curationController.getRecommendations(keywordId, userDetails);

      // then
      assertNotNull(response.getBody());
      assertTrue(response.getBody().isEmpty());

      verify(curationService).getRecommendationsByKeyword(keywordId, userId);
    }

    @Test
    @DisplayName("사용자 인증 정보로부터 userId 추출")
    void extractsUserIdFromUserDetails() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();

      List<Content> mockRecommendations = List.of();

      when(userDetails.getUsername()).thenReturn(userId.toString());
      when(curationService.getRecommendationsByKeyword(keywordId, userId)).thenReturn(mockRecommendations);

      // when
      ResponseEntity<List<Content>> response = curationController.getRecommendations(keywordId, userDetails);

      // then
      verify(userDetails).getUsername();
      verify(curationService).getRecommendationsByKeyword(keywordId, userId);
    }
  }

  @Nested
  @DisplayName("키워드 삭제 요청")
  class DeleteKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID keywordId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      // when
      ResponseEntity<Void> response = curationController.delete(keywordId, userDetails);

      // then
      assertEquals(204, response.getStatusCodeValue()); // NO_CONTENT
      assertNull(response.getBody());

      verify(curationService).delete(keywordId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 키워드 삭제 시도")
    void failsWhenKeywordNotFound() {
      // given
      UUID keywordId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      // void 메서드에 대한 예외 설정은 doThrow 사용
      doThrow(new KeywordNotFoundException()).when(curationService).delete(keywordId, userId);

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> curationController.delete(keywordId, userDetails));
    }
  }
}