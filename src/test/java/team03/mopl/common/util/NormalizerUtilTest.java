package team03.mopl.common.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("NormalizerUtil 테스트")
class NormalizerUtilTest {

  @DisplayName("다양한 문자열 입력에 대해 정규화된 결과 반환")
  @ParameterizedTest
  @CsvSource(value = {
      // 한국어 기반 단어
      "'케이팝 데몬 헌터스', '케이팝데몬헌터스'",
      // 영어 기반 단어
      "'제이컵을 위하여' - Defending Jacob, '제이컵을위하여defendingjacob'",
      // 한자가 포함된 단어
      "'무명 無名', '무명無名'",
      // 숫자가 포함된 단어
      "'English Premier League 2022-08-06 Fulham vs Liverpool', 'englishpremierleague20220806fulhamvsliverpool'",
      // 다이어크리틱스 포함 단어
      "'Résumé', 'resume'",
      // 특수 문자 포함
      "''세브란스: 단절' - Severance', '세브란스단절severance'",
      "'이재, 곧 죽습니다', '이재곧죽습니다'"
  })
  void shouldReturnNormalizedResult(String input, String expected) {
    // when
    String actual = NormalizerUtil.normalize(input);
    // then
    assertEquals(expected, actual);
  }

  @Test
  @DisplayName("빈 문자열 입력 시 빈 문자열 반환")
  void shouldReturnEmptyResultWithEmpty() {
    // when
    String actual = NormalizerUtil.normalize("");
    // then
    assertEquals("", actual);
  }

  @Test
  @DisplayName("null 입력 시 빈 문자열 반환")
  void shouldReturnEmptyResultWithNull() {
    // when
    String actual = NormalizerUtil.normalize(null);
    // then
    assertEquals("", actual);
  }

}
