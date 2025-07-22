package team03.mopl.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import team03.mopl.common.dto.Cursor;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;

@DisplayName("CursorCodecUtil 테스트")
class CursorCodecUtilTest {

  private ObjectMapper objectMapper;
  private CursorCodecUtil codecUtil;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    codecUtil = new CursorCodecUtil(objectMapper);
  }

  @Nested
  @DisplayName("디코딩 테스트")
  class decoder {

    @Test
    @DisplayName("유효한 Base64 인코딩 문자열이 주어졌을 때 Cursor 객체 반환")
    void shouldReturnCursorResult() throws JsonProcessingException {
      // given
      Cursor cursor = new Cursor("테스트 Value", UUID.randomUUID().toString());
      String encodedCursor = Base64.getUrlEncoder().encodeToString(
          objectMapper.writeValueAsString(cursor).getBytes(StandardCharsets.UTF_8)
      );

      // when
      Cursor decodedCursor = codecUtil.decodeCursor(encodedCursor);

      // then
      assertNotNull(decodedCursor);
      assertEquals(cursor.lastValue(), decodedCursor.lastValue());
      assertEquals(cursor.lastId(), decodedCursor.lastId());
    }

    @Test
    @DisplayName("null 입력 시 기본 커서 객체 반환")
    void shouldReturnDefaultCursorWithNull() {
      // when
      Cursor decodedCursor = codecUtil.decodeCursor(null);

      // then
      assertNotNull(decodedCursor);
      assertNull(decodedCursor.lastValue());
      assertNull(decodedCursor.lastId());
    }

    @Test
    @DisplayName("유효하지 않은 문자열 입력 시 기본 커서 객체 반환")
    void shouldReturnDefaultCursorWithInvalid() throws JsonProcessingException {
      // given
      String invalidValue = "123@!!!!";

      // when
      Cursor decodedCursor = codecUtil.decodeCursor(invalidValue);

      // then
      assertNotNull(decodedCursor);
      assertNull(decodedCursor.lastValue());
      assertNull(decodedCursor.lastId());
    }
  }

  @Nested
  @DisplayName("인코딩 테스트")
  class encoder {

    @Test
    @DisplayName("ContentDto를 인코딩하여 Base64 문자열 반환")
    void shouldReturnCursorStringResultWithContentDto() {
      // given
      UUID testId = UUID.randomUUID();
      LocalDateTime testDate = LocalDateTime.now();

      ContentDto lastItem = new ContentDto(
          testId,
          testDate,
          "테스트 컨텐츠",
          "테스트컨텐츠",
          "콘텐츠 설명",
          ContentType.MOVIE,
          testDate.minusDays(1),
          "https://youtube.com/watch?v=test",
          "https://image.png",
          BigDecimal.valueOf(2.5)
      );

      // when
      String encodedSortByTitle = codecUtil.encodeNextCursor(lastItem, "TITLE");
      String encodedSortByReleaseDate = codecUtil.encodeNextCursor(lastItem, "RELEASE_AT");
      String encodedSortByRating = codecUtil.encodeNextCursor(lastItem, "AVG_RATING");

      Cursor decodedSortByTitle = codecUtil.decodeCursor(encodedSortByTitle);
      Cursor decodedSortByReleaseDate = codecUtil.decodeCursor(encodedSortByReleaseDate);
      Cursor decodedSortByRating = codecUtil.decodeCursor(encodedSortByRating);

      // then
      assertEquals("테스트컨텐츠", decodedSortByTitle.lastValue());
      assertEquals(testDate.minusDays(1).toString(), decodedSortByReleaseDate.lastValue());
      assertEquals(BigDecimal.valueOf(2.5).toString(), decodedSortByRating.lastValue());
    }

    @Test
    @DisplayName("WatchRoomDto를 인코딩하여 Base64 문자열 반환")
    void shouldReturnCursorStringResultWithWatchRoomDto() {
      // given
      UUID testId = UUID.randomUUID();
      LocalDateTime testDate = LocalDateTime.now();

      WatchRoomDto lastItem = new WatchRoomDto(
          testId,
          "테스트 시청방",
          "테스트 컨텐츠",
          UUID.randomUUID(),
          "테스트 방장",
          testDate,
          1L
      );

      // when
      String encodedSortByCreatedAt = codecUtil.encodeNextCursor(lastItem, "createdat");
      String encodedSortByTitle = codecUtil.encodeNextCursor(lastItem, "title");
      String encodedSortByParticipantcount = codecUtil.encodeNextCursor(lastItem,
          "participantcount");

      Cursor decodedSortByCreatedAt = codecUtil.decodeCursor(encodedSortByCreatedAt);
      Cursor decodedSortByTitle = codecUtil.decodeCursor(encodedSortByTitle);
      Cursor decodedSortByParticipantcount = codecUtil.decodeCursor(encodedSortByParticipantcount);

      // then
      assertEquals("테스트 시청방", decodedSortByTitle.lastValue());
      assertEquals(testDate.toString(), decodedSortByCreatedAt.lastValue());
      assertEquals("1", decodedSortByParticipantcount.lastValue());
    }
    
    @Test
    @DisplayName("null 입력될 시 기본값 TITLE 기준 기본 커서 객체 생성")
    void shouldReturnSortByTitleResultWithNull(){
      // given
      UUID testId = UUID.randomUUID();
      LocalDateTime testDate = LocalDateTime.now();

      ContentDto lastItem = new ContentDto(
          testId,
          testDate,
          "테스트 컨텐츠",
          "테스트컨텐츠",
          "콘텐츠 설명",
          ContentType.MOVIE,
          testDate.minusDays(1),
          "https://youtube.com/watch?v=test",
          "https://image.png",
          BigDecimal.valueOf(2.5)
      );

      // when
      String encodedSortByTitle = codecUtil.encodeNextCursor(lastItem, null);
      Cursor decodedSortByTitle = codecUtil.decodeCursor(encodedSortByTitle);

      // then
      assertEquals("테스트컨텐츠", decodedSortByTitle.lastValue());
    }

    @Test
    @DisplayName("null 입력될 시 기본값 participantcount 기준 기본 커서 객체 생성")
    void shouldReturnSortByParticipantcountResultWithNull(){
      // given
      UUID testId = UUID.randomUUID();
      LocalDateTime testDate = LocalDateTime.now();

      WatchRoomDto lastItem = new WatchRoomDto(
          testId,
          "테스트 시청방",
          "테스트 컨텐츠",
          UUID.randomUUID(),
          "테스트 방장",
          testDate,
          1L
      );

      // when
      String encodedSortByParticipantcount = codecUtil.encodeNextCursor(lastItem, null);
      Cursor decodedSortByParticipantcount = codecUtil.decodeCursor(encodedSortByParticipantcount);

      // then
      assertEquals("1", decodedSortByParticipantcount.lastValue());
    }

    @Test
    @DisplayName("지원하지 않는 정렬 기준으로 인코딩시 에러 반환")
    void shouldReturnErrorWithInvalid() {
      // given
      UUID testId = UUID.randomUUID();
      LocalDateTime testDate = LocalDateTime.now();

      WatchRoomDto lastItem = new WatchRoomDto(
          testId,
          "테스트 시청방",
          "테스트 컨텐츠",
          UUID.randomUUID(),
          "테스트 방장",
          testDate,
          1L
      );
      String invalidSortBy = "invalidSortBy";

      // when & then
      assertThrows(IllegalArgumentException.class,
          () -> codecUtil.encodeNextCursor(lastItem, invalidSortBy));
    }
  }

}
