package team03.mopl.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.review.dto.ReviewDto;
import team03.mopl.domain.review.service.ReviewService;
import team03.mopl.domain.review.service.ReviewServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("컨텐츠 데이터 서비스 단위 테스트")
class ContentServiceImplTest {

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ReviewServiceImpl reviewService;

  @Mock
  private ObjectMapper objectMapper;

  @InjectMocks
  private ContentServiceImpl contentService;


  @Nested
  @DisplayName("컨텐츠 데이터 조회")
  class getContent {

    @Test
    @DisplayName("컨텐츠 데이터 조회 성공")
    void success() {
      // given
      UUID id = UUID.randomUUID();
      Content content = Content.builder()
          .title("테스트 컨텐츠")
          .description("설명")
          .contentType(ContentType.MOVIE)
          .releaseDate(LocalDateTime.now())
          .build();

      when(contentRepository.findById(id)).thenReturn(Optional.of(content));

      // when
      ContentDto result = contentService.getContent(id);

      // then
      assertThat(result.title()).isEqualTo("테스트 컨텐츠");
      assertThat(result.description()).isEqualTo("설명");
      assertThat(result.contentType()).isEqualTo(ContentType.MOVIE);

      verify(contentRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("컨텐츠 데이터 조회 실패 - 존재하지 않는 컨텐츠")
    void fail_WhenNoContent() {
      //given
      UUID nonExistingid = UUID.randomUUID();
      when(contentRepository.findById(nonExistingid)).thenReturn(Optional.empty());

      // when, then
      assertThrows(ContentNotFoundException.class, () -> contentService.getContent(nonExistingid));

      verify(contentRepository, times(1)).findById(nonExistingid);
    }
  }

  @Nested
  @DisplayName("커서 페이지네이션 기반 컨텐츠 데이터 목록 조회")
  class getContentWithCursor {

    @Test
    @DisplayName("첫 페이지 조회 시, 다음 페이지가 있으면 hasNext=true, nextCursor가 생성된다.")
    void getCursorPage_WhenFirstPageAndHasNext() throws JsonProcessingException {
      // given
      ContentSearchRequest request = new ContentSearchRequest(null, null, "TITLE", "DESC", null, 5);

      List<Content> contents = new ArrayList<>();
      for (int i = 0; i < request.getSize() + 1; i++) {

        Content content = Content.builder()
            .title("title" + i)
            .titleNormalized("title" + i)
            .releaseDate(LocalDateTime.parse("2025-07-07T10:00"))
            .contentType(ContentType.MOVIE)
            .build();

        try {
          // 리플렉션
          Field idField = Content.class.getDeclaredField("id");
          idField.setAccessible(true);
          idField.set(content, UUID.randomUUID());
        } catch (Exception e) {
        }

        contents.add(content);
      }

      when(contentRepository.findContentsWithCursor(
          any(), any(), anyString(), anyString(), any(), any(), anyInt()
      )).thenReturn(contents);

      when(objectMapper.writeValueAsString(any(Cursor.class))).thenReturn(
          "{\"lastId\":\"some-id\",\"lastValue\":\"title+some-num\"}"
      );

      // when
      CursorPageResponseDto<ContentDto> response = contentService.getCursorPage(request);

      // then
      assertThat(response).isNotNull();
      assertTrue(response.hasNext());
      assertThat(response.size()).isEqualTo(request.getSize());
      assertThat(response.nextCursor()).isNotNull().isNotBlank();
    }
  }

  @Nested
  @DisplayName("커서 페이지네이션 기반 컨텐츠 데이터 목록 조회")
  class updateContentRating {

    @Test
    @DisplayName("성공 - 리뷰들의 평균 평점이 정상적으로 계산되어 업데이트 ")
    void success_updateRating() {
      // given
      UUID id = UUID.randomUUID();
      Content content = Content.builder().build();

      List<ReviewDto> reviews = List.of(
          new ReviewDto(UUID.randomUUID(), UUID.randomUUID(), "테스트 작성자1", "리뷰 테스트 제목1", "리뷰 테스트 본문1",
              LocalDateTime.now(), new BigDecimal("4.0")),
          new ReviewDto(UUID.randomUUID(), UUID.randomUUID(), "테스트 작성자2", "리뷰 테스트 제목2", "리뷰 테스트 본문2",
              LocalDateTime.now(), new BigDecimal("3.0"))
      );

      when(reviewService.getAllByContent(any(UUID.class))).thenReturn(reviews);
      when(contentRepository.findById(any(UUID.class))).thenReturn(Optional.of(content));

      // when
      contentService.updateContentRating(id);

      // then
      assertEquals(new BigDecimal("3.50"), content.getAvgRating());
      verify(reviewService, times(1)).getAllByContent(any(UUID.class));
      verify(contentRepository, times(1)).findById(any(UUID.class));
      verify(contentRepository, times(1)).save(content);
    }
  }
}
