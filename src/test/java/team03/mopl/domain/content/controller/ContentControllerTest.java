package team03.mopl.domain.content.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;
import team03.mopl.domain.content.service.ContentService;
import team03.mopl.domain.review.service.ReviewService;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ContentController.class)
@DisplayName("컨텐츠 데이터 컨트롤러 단위 테스트")
class ContentControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  private ContentService contentService;
  @MockitoBean
  private ReviewService reviewService;

  @Nested
  @DisplayName("특정 컨텐츠 조회")
  class getContent {

    @Test
    @DisplayName("성공 - 데이터 있음")
    void returnAllContentWhenExists() throws Exception {
      // given
      Content content = Content.builder()
          .title("테스트 컨텐츠")
          .titleNormalized("테스트컨텐츠")
          .description("테스트 설명")
          .contentType(ContentType.MOVIE)
          .youtubeUrl("")
          .build();

      ContentDto contentDto = ContentDto.from(content);

      when(contentService.getContent(any(UUID.class))).thenReturn(contentDto);

      // when
      ResultActions actions = mockMvc.perform(get("/api/contents/{id}", UUID.randomUUID()));

      // then
      actions
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("테스트 컨텐츠"))
          .andExpect(jsonPath("$.titleNormalized").value("테스트컨텐츠"))
          .andExpect(jsonPath("$.description").value("테스트 설명"))
          .andExpect(jsonPath("$.contentType").value("MOVIE"))
          .andExpect(jsonPath("$.youtubeUrl").value(""));
    }

    @Test
    @DisplayName("실패 - 데이터 없음")
    void returnNotFoundIfContentMissing() throws Exception {
      // given
      when(contentService.getContent(any(UUID.class))).thenThrow(new ContentNotFoundException());

      // when
      ResultActions actions = mockMvc.perform(get("/api/contents/{id}", UUID.randomUUID()));

      // then
      actions.andExpect(status().isNotFound());
    }
  }


  @Nested
  @DisplayName("전체 컨텐츠 데이터 조회")
  class getContents {

    @Test
    @DisplayName("성공 - 전체 데이터를 조회")
    void returnContentsAllIfExists() throws Exception {
      // given
      Content content1 = Content.builder()
          .title("테스트 컨텐츠1")
          .titleNormalized("테스트컨텐츠1")
          .description("테스트 설명1")
          .contentType(ContentType.MOVIE)
          .youtubeUrl("")
          .build();
      Content content2 = Content.builder()
          .title("테스트 컨텐츠2")
          .titleNormalized("테스트컨텐츠2")
          .description("테스트 설명2")
          .contentType(ContentType.SPORTS)
          .youtubeUrl("")
          .build();
      ContentDto contentDto1 = ContentDto.from(content1);
      ContentDto contentDto2 = ContentDto.from(content2);
      List<ContentDto> contentsDtos = List.of(contentDto1, contentDto2);

      CursorPageResponseDto cursorPageResponseDto =
          CursorPageResponseDto.<ContentDto>builder()
              .data(contentsDtos)
              .nextCursor(null)
              .size(2)
              .totalElements(2L)
              .hasNext(false)
              .build();

      when(contentService.getCursorPage(any(ContentSearchRequest.class))).thenReturn(
          cursorPageResponseDto);

      // when
      ResultActions actions = mockMvc.perform(get("/api/contents"));

      // then
      actions
          .andDo(print())
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.data[0].title").value("테스트 컨텐츠1"))
          .andExpect(jsonPath("$.data[0].description").value("테스트 설명1"))
          .andExpect(jsonPath("$.data[0].contentType").value(ContentType.MOVIE.toString()))
          .andExpect(jsonPath("$.data[1].title").value("테스트 컨텐츠2"))
          .andExpect(jsonPath("$.data[1].description").value("테스트 설명2"))
          .andExpect(jsonPath("$.data[1].contentType").value(ContentType.SPORTS.toString()))
          .andExpect(jsonPath("$.size").value(2))
          .andExpect(jsonPath("$.totalElements").value(2L));
    }


    @Test
    @DisplayName("성공 - 전체 데이터 조회시 빈 CursorPageResponseDto 반환 ")
    void returnEmptyResultIfNoContents() throws Exception {
      // given
      CursorPageResponseDto cursorPageResponseDto =
          CursorPageResponseDto.<ContentDto>builder()
              .data(new ArrayList<>())
              .nextCursor(null)
              .totalElements(0L)
              .hasNext(false)
              .build();
      when(contentService.getCursorPage(any(ContentSearchRequest.class))).thenReturn(
          cursorPageResponseDto);

      // when
      ResultActions actions = mockMvc.perform(get("/api/contents"));

      // then
      actions
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.totalElements").value(0L));
    }
  }
}
