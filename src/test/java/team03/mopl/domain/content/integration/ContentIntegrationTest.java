package team03.mopl.domain.content.integration;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@WithMockUser
@DisplayName("컨텐츠 통합 테스트")
public class ContentIntegrationTest {

//  @Autowired
//  private MockMvc mockMvc;
//
//  @Autowired
//  private ContentRepository contentRepository;
//
//  @Nested
//  @DisplayName("콘텐츠 데이터 조회")
//  class getContent{
//    @Test
//    @DisplayName("성공 - 존재하는 ID로 컨텐츠 조회시 200 OK, 컨텐츠 정보 반환")
//    void getContent_success() throws Exception {
//      // given
//      Content savedContent = contentRepository.save( Content.builder()
//          .title("통합 테스트용 컨텐츠")
//          .description("설명")
//          .contentType(ContentType.MOVIE)
//          .releaseDate(LocalDateTime.now())
//          .build()
//      );
//      UUID saveId = savedContent.getId();
//
//      // when
//      ResultActions actions = mockMvc.perform(get("/api/contents/{contentId}", saveId));
//
//      // then
//      actions
//          .andExpect(status().isOk())
//          .andExpect(jsonPath("$.id").value(saveId.toString()))
//          .andExpect(jsonPath("$.title").value("통합 테스트용 컨텐츠"))
//          .andExpect(jsonPath("$.description").value("설명"));
//    }
//
//    @Test
//    @DisplayName("성공 - 존재하지 않는 ID로 컨텐츠 조회시 400 Not Found 반환")
//    void getContent_Fail() throws Exception {
//      // given
//      UUID nonExistingId = UUID.randomUUID();
//
//      // when
//      ResultActions actions = mockMvc.perform(get("/api/contents/{contentId}", nonExistingId));
//
//      // then
//      actions
//          .andExpect(status().isNotFound());
//    }
//  }

}
