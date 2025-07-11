package team03.mopl.common.util;

import java.text.Normalizer;
import java.text.Normalizer.Form;

/**
 * 문자열을 검색이나 정렬에 사용하기 알맞는 형태로 정규화하는 유틸리티 클래스입니다.
 * <p>
 * 이 클래스는 대소문자, 악센트, 특수기호, 공백 등 정렬 순서에 영향을 줄 수 있는 요소들을 제거하여 일관된 비교가 가능한 정규화 키를 생성합니다.
 * <br>
 * e.g.
 * 1. Résume -> Resume
 * 2. K-POP Devil's -> kpopdevils
 */
public class NormalizerUtil {

  /**
   * @param input 정규화할 원본 문자열
   * @return 정규화된 문자열. input이 null이거나 비어있을 경우 빈 문자열("")을 반환합니다.
   * */
  public static String normalize(String input) {
    if (input == null || input.isEmpty()) {
      return "";
    }

    // 1. 다이어크리틱스 제거 -> input: é -> return: e, `
    String normalized = Normalizer.normalize(input, Form.NFD);
    // 2. 다이어크리틱스의 악센트 제거
    // \\p{InCombiningDiacriticalMarks}: 유니코드의 모든 결합 악센트 문자
    normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

    // 3. 분해됐던 한글 자모를 완성형 한글로 합친다.
    normalized = Normalizer.normalize(normalized, Form.NFC);

    // 4. 소문자로 변경
    normalized = normalized.toLowerCase();

    // 5. 한글, 알파벳, 한자, 숫자 제외 모든 특수문자와 공백 제거
    normalized = normalized.replaceAll("[^a-z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\p{IsHan}]", "");

    return normalized;
  }
}
