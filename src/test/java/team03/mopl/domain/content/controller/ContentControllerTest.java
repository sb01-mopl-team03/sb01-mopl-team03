package team03.mopl.domain.content.controller;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.service.ContentService;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ContentController.class)
@DisplayName("컨텐츠 데이터 컨트롤러 단위 테스트")
class ContentControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @MockitoBean
  private ContentService contentService;

  @Nested
  @DisplayName("전체 컨텐츠 데이터 조회")
  class getContents {

//    @Test
//    @DisplayName("성공 - 데이터 있음")
//    void success() throws Exception {
//      // given
//      Content content1 = Content.builder()
//          .title("테스트 컨텐츠1")
//          .description("설명1")
//          .contentType(ContentType.MOVIE)
//          .releaseDate(LocalDateTime.now())
//          .build();
//      Content content2 = Content.builder()
//          .title("테스트 컨텐츠2")
//          .description("설명2")
//          .contentType(ContentType.SPORTS)
//          .releaseDate(LocalDateTime.now())
//          .build();
//      ContentDto contentDto1 = ContentDto.from(content1);
//      ContentDto contentDto2 = ContentDto.from(content2);
//      List<ContentDto> contentsDtos = List.of(contentDto1, contentDto2);
//
//      when(contentService.getAll()).thenReturn(contentsDtos);
//
//      // when
//      ResultActions actions = mockMvc.perform(get("/api/contents"));
//
//      // then
//      actions
//          .andExpect(status().isOk())
//          .andExpect(jsonPath("$.length()").value(2))
//          .andExpect(jsonPath("$[0].title").value("테스트 컨텐츠1"))
//          .andExpect(jsonPath("$[0].description").value("설명1"))
//          .andExpect(jsonPath("$[0].contentType").value(ContentType.MOVIE.toString()))
//          .andExpect(jsonPath("$[1].title").value("테스트 컨텐츠2"))
//          .andExpect(jsonPath("$[1].description").value("설명2"))
//          .andExpect(jsonPath("$[1].contentType").value(ContentType.SPORTS.toString()));
//    }
//
//
//    @Test
//    @DisplayName("성공 - 데이터 없음")
//    void success_whenNoContent() throws Exception {
//      // given
//      when(contentService.getAll()).thenReturn(Collections.emptyList());
//
//      // when
//      ResultActions actions = mockMvc.perform(get("/api/contents"));
//
//      // then
//      actions
//          .andExpect(status().isOk())
//          .andExpect(jsonPath("$.length()").value(0));
//    }
  }
}


