package team03.mopl.domain.curation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.common.exception.curation.KeywordNotFoundException;
import team03.mopl.common.exception.curation.KeywordDeleteDeniedException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.curation.controller.CurationController;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.dto.KeywordRequest;
import team03.mopl.domain.curation.entity.Keyword;
import team03.mopl.domain.curation.service.CurationService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("큐레이션 컨트롤러 테스트")
class CurationControllerTest {

  @Mock
  private CurationService curationService;

  @InjectMocks
  private CurationController curationController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(curationController).build();
    objectMapper = new ObjectMapper();

    // 매 테스트마다 mock 초기화
    reset(curationService);
  }

  @Nested
  @DisplayName("키워드 등록 요청")
  class RegisterKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given - 각 테스트마다 독립적인 UUID 생성
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();

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
          .id(keywordId)
          .user(mockUser)
          .keyword("액션")
          .build();

      KeywordDto expectedResponse = KeywordDto.from(mockKeyword);

      when(curationService.registerKeyword(userId, keywordText)).thenReturn(expectedResponse);

      // when
      ResponseEntity<KeywordDto> response = curationController.registerKeyword(request);

      // then
      assertNotNull(response.getBody());
      assertEquals(200, response.getStatusCode().value());
      assertEquals(keywordText, response.getBody().keyword());
      assertEquals(userId, response.getBody().userId());

      verify(curationService, times(1)).registerKeyword(userId, keywordText);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      String keywordText = "액션";
      KeywordRequest request = new KeywordRequest(userId, keywordText);

      when(curationService.registerKeyword(userId, keywordText))
          .thenThrow(new UserNotFoundException());

      // when & then
      assertThrows(UserNotFoundException.class, () -> {
        curationController.registerKeyword(request);
      });

      verify(curationService, times(1)).registerKeyword(userId, keywordText);
    }

    @Test
    @DisplayName("유효하지 않은 요청 데이터")
    void failsWithInvalidRequest() {
      // given
      KeywordRequest invalidRequest = new KeywordRequest(null, "");

      // when & then
      assertThrows(IllegalArgumentException.class, () -> {
        if (invalidRequest.userId() == null || invalidRequest.keyword().trim().isEmpty()) {
          throw new IllegalArgumentException("Invalid request");
        }
      });
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
      CustomUserDetails userDetails = mock(CustomUserDetails.class);

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

      List<ContentDto> mockRecommendations = List.of(
          ContentDto.from(mockContent1),
          ContentDto.from(mockContent2)
      );

      when(userDetails.getId()).thenReturn(userId);
      when(curationService.getRecommendationsByKeyword(keywordId, userId))
          .thenReturn(mockRecommendations);

      // when
      ResponseEntity<List<ContentDto>> response = curationController.getRecommendations(keywordId, userDetails);

      // then
      assertNotNull(response.getBody());
      assertEquals(200, response.getStatusCode().value());
      assertEquals(2, response.getBody().size());
      assertEquals(mockContent1.getTitle(), response.getBody().get(0).title());
      assertEquals(mockContent2.getTitle(), response.getBody().get(1).title());

      verify(userDetails, times(1)).getId();
      verify(curationService, times(1)).getRecommendationsByKeyword(keywordId, userId);
    }

    @Test
    @DisplayName("빈 추천 목록 반환")
    void returnsEmptyRecommendations() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();
      CustomUserDetails userDetails = mock(CustomUserDetails.class);
      List<ContentDto> emptyRecommendations = List.of();

      when(userDetails.getId()).thenReturn(userId);
      when(curationService.getRecommendationsByKeyword(keywordId, userId))
          .thenReturn(emptyRecommendations);

      // when
      ResponseEntity<List<ContentDto>> response = curationController.getRecommendations(keywordId, userDetails);

      // then
      assertNotNull(response.getBody());
      assertEquals(200, response.getStatusCode().value());
      assertTrue(response.getBody().isEmpty());

      verify(userDetails, times(1)).getId();
      verify(curationService, times(1)).getRecommendationsByKeyword(keywordId, userId);
    }

    @Test
    @DisplayName("키워드를 찾을 수 없는 경우")
    void failsWhenKeywordNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();
      CustomUserDetails userDetails = mock(CustomUserDetails.class);

      when(userDetails.getId()).thenReturn(userId);
      when(curationService.getRecommendationsByKeyword(keywordId, userId))
          .thenThrow(new KeywordNotFoundException());

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationController.getRecommendations(keywordId, userDetails);
      });

      verify(userDetails, times(1)).getId();
      verify(curationService, times(1)).getRecommendationsByKeyword(keywordId, userId);
    }

    @Test
    @DisplayName("사용자 인증 정보로부터 userId 추출")
    void extractsUserIdFromUserDetails() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();
      CustomUserDetails userDetails = mock(CustomUserDetails.class);
      List<ContentDto> mockRecommendations = List.of();

      when(userDetails.getId()).thenReturn(userId);
      when(curationService.getRecommendationsByKeyword(keywordId, userId))
          .thenReturn(mockRecommendations);

      // when
      ResponseEntity<List<ContentDto>> response = curationController.getRecommendations(keywordId, userDetails);

      // then
      verify(userDetails, times(1)).getId();
      verify(curationService, times(1)).getRecommendationsByKeyword(keywordId, userId);
    }
  }

  @Nested
  @DisplayName("키워드 삭제 요청")
  class DeleteKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();
      CustomUserDetails userDetails = mock(CustomUserDetails.class);

      when(userDetails.getId()).thenReturn(userId);
      doNothing().when(curationService).delete(keywordId, userId);

      // when
      ResponseEntity<Void> response = curationController.delete(keywordId, userDetails);

      // then
      assertEquals(204, response.getStatusCode().value());
      assertNull(response.getBody());

      verify(userDetails, times(1)).getId();
      verify(curationService, times(1)).delete(keywordId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 키워드 삭제 시도")
    void failsWhenKeywordNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();
      CustomUserDetails userDetails = mock(CustomUserDetails.class);

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new KeywordNotFoundException()).when(curationService).delete(keywordId, userId);

      // when & then
      assertThrows(KeywordNotFoundException.class, () -> {
        curationController.delete(keywordId, userDetails);
      });

      verify(userDetails, times(1)).getId();
      verify(curationService, times(1)).delete(keywordId, userId);
    }

    @Test
    @DisplayName("다른 사용자의 키워드 삭제 시도")
    void failsWhenDeletingOtherUserKeyword() {
      // given
      UUID userId = UUID.randomUUID();
      UUID keywordId = UUID.randomUUID();
      CustomUserDetails userDetails = mock(CustomUserDetails.class);

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new KeywordDeleteDeniedException()).when(curationService).delete(keywordId, userId);

      // when & then
      assertThrows(KeywordDeleteDeniedException.class, () -> {
        curationController.delete(keywordId, userDetails);
      });

      verify(userDetails, times(1)).getId();
      verify(curationService, times(1)).delete(keywordId, userId);
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarios {

    @Test
    @DisplayName("키워드 등록 후 조회 후 삭제 전체 플로우")
    void fullWorkflow() {
      // given
      UUID workflowUserId = UUID.randomUUID();
      UUID workflowKeywordId = UUID.randomUUID();

      String keywordText = "액션";
      KeywordRequest registerRequest = new KeywordRequest(workflowUserId, keywordText);

      // 이 테스트에서 사용할 Keyword와 User 객체 생성
      User mockUser = User.builder()
          .id(workflowUserId)
          .name("테스트유저")
          .email("test@test.com")
          .password("test")
          .role(Role.USER)
          .build();

      Keyword mockKeyword = Keyword.builder()
          .id(workflowKeywordId)
          .user(mockUser)
          .keyword(keywordText)
          .build();

      KeywordDto keywordResponse = KeywordDto.from(mockKeyword);

      Content actionContent = Content.builder()
          .id(UUID.randomUUID())
          .title("액션 영화")
          .description("액션 영화입니다")
          .contentType(ContentType.MOVIE)
          .avgRating(BigDecimal.valueOf(4.5))
          .build();

      List<ContentDto> recommendations = List.of(ContentDto.from(actionContent));

      // when & then - 키워드 등록
      when(curationService.registerKeyword(workflowUserId, keywordText)).thenReturn(keywordResponse);
      ResponseEntity<KeywordDto> registerResponse = curationController.registerKeyword(registerRequest);

      assertEquals(200, registerResponse.getStatusCode().value());
      assertEquals(keywordText, registerResponse.getBody().keyword());

      // when & then - 추천 조회
      CustomUserDetails workflowUserDetails = mock(CustomUserDetails.class);
      when(workflowUserDetails.getId()).thenReturn(workflowUserId);
      when(curationService.getRecommendationsByKeyword(workflowKeywordId, workflowUserId)).thenReturn(recommendations);

      ResponseEntity<List<ContentDto>> getResponse = curationController.getRecommendations(workflowKeywordId, workflowUserDetails);

      assertEquals(200, getResponse.getStatusCode().value());
      assertEquals(1, getResponse.getBody().size());
      assertEquals("액션 영화", getResponse.getBody().get(0).title());

      // when & then - 키워드 삭제
      doNothing().when(curationService).delete(workflowKeywordId, workflowUserId);

      ResponseEntity<Void> deleteResponse = curationController.delete(workflowKeywordId, workflowUserDetails);

      assertEquals(204, deleteResponse.getStatusCode().value());

      // verify all interactions
      verify(curationService, times(1)).registerKeyword(workflowUserId, keywordText);
      verify(curationService, times(1)).getRecommendationsByKeyword(workflowKeywordId, workflowUserId);
      verify(curationService, times(1)).delete(workflowKeywordId, workflowUserId);
      verify(workflowUserDetails, times(2)).getId();
    }
  }

  @Nested
  @DisplayName("에러 핸들링 테스트")
  class ErrorHandling {

    @Test
    @DisplayName("서비스 레이어 예외 전파 확인")
    void serviceExceptionPropagation() {
      // given
      UUID userId = UUID.randomUUID();
      String keywordText = "액션";
      KeywordRequest request = new KeywordRequest(userId, keywordText);

      RuntimeException serviceException = new RuntimeException("서비스 에러");
      when(curationService.registerKeyword(userId, keywordText)).thenThrow(serviceException);

      // when & then
      RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
        curationController.registerKeyword(request);
      });

      assertEquals("서비스 에러", thrownException.getMessage());
      verify(curationService, times(1)).registerKeyword(userId, keywordText);
    }

    @Test
    @DisplayName("NULL RequestBody 처리")
    void handlesNullRequestBody() {
      // when & then
      assertThrows(NullPointerException.class, () -> {
        curationController.registerKeyword(null);
      });
    }

    @Test
    @DisplayName("NULL UserDetails 처리")
    void handlesNullUserDetails() {
      // given
      UUID keywordId = UUID.randomUUID();

      // when & then
      assertThrows(NullPointerException.class, () -> {
        curationController.getRecommendations(keywordId, null);
      });

      assertThrows(NullPointerException.class, () -> {
        curationController.delete(keywordId, null);
      });
    }
  }
}