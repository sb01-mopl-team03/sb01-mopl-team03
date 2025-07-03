package team03.mopl.domain.content.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.repository.ContentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("컨텐츠 데이터 서비스 단위 테스트")
class ContentServiceImplTest {

  @Mock
  private ContentRepository contentRepository;

  @InjectMocks
  private ContentServiceImpl contentService;

  @Nested
  @DisplayName("컨텐츠 데이터 조회")
  class getContent {

    @Test
    @DisplayName("컨텐츠 데이터 조회 성공")
    void success(){
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
    void fail_WhenNoContent(){
      //given
      UUID nonExistingid = UUID.randomUUID();
      when(contentRepository.findById(nonExistingid)).thenReturn(Optional.empty());

      // when, then
      assertThrows(ContentNotFoundException.class, () -> contentService.getContent(nonExistingid));

      verify(contentRepository, times(1)).findById(nonExistingid);
    }
  }
}