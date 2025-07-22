package team03.mopl.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import team03.mopl.common.dto.Cursor;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class CursorCodecUtil {

  public final ObjectMapper objectMapper;

  /**
   * 인코딩된 cursor 값을 디코딩합니다.
   *
   * @param encodedCursor 인코딩된 문자열 값
   */
  public Cursor decodeCursor(String encodedCursor) {
    log.info("decodeCursor - cursor 값 Base64 디코딩 시작");
    if (encodedCursor == null) {
      log.warn("null 입력으로 기본 커서 객체를 반환");
      return new Cursor(null, null);
    }
    try {
      // 1. Base64 문자열 → Byte 변환
      byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedCursor);
      // 2. Byte → JSON 변환
      String decodedJson = new String(decodedBytes, StandardCharsets.UTF_8);
      // 4. JSON → Cursor 객체 변환및 반환
      return objectMapper.readValue(decodedJson, Cursor.class);
    } catch (Exception e) {
      log.warn("Base64 문자열을 디코딩하여 객체로 변환 중 오류 발생", e);
      return new Cursor(null, null);
    }
  }

  /**
   * 커서 페이지네이션의 마지막 데이터를 인코딩하여 반환합니다.
   * 다른 서비스에서 호출됩니다.
   *
   * @param lastItem ContentDto 타입의 아이템
   * @param sortBy   value 값을 좌우하는 정렬 기준
   */
  public String encodeNextCursor(ContentDto lastItem, String sortBy) {
    log.info("encodeNextCursor - 커서 페이지네이션의 마지막 데이터 Base64 인코딩 시작");

    String lastId = lastItem.id().toString();
    String upperSortBy = !(sortBy == null) ? sortBy.toUpperCase() : "TITLE";
    String lastValue;
    switch (upperSortBy) {
      case "RELEASE_AT" -> lastValue = lastItem.releaseDate().toString();
      case "TITLE" -> lastValue = lastItem.titleNormalized();
      case "AVG_RATING" -> lastValue = lastItem.avgRating().toString();
      default -> throw new IllegalArgumentException("지원하지 않는 정렬 방식: " + sortBy);
    }
    Cursor cursor = new Cursor(lastValue, lastId);
    return encodeNextCursor(cursor);
  }

  /**
   * 커서 페이지네이션의 마지막 데이터를 인코딩하여 반환합니다.
   * 다른 서비스에서 호출됩니다.
   *
   * @param lastItem WatchRoomDto 타입의 아이템
   * @param sortBy   value 값을 좌우하는 정렬 기준
   */
  public String encodeNextCursor(WatchRoomDto lastItem, String sortBy) {
    log.info("encodeNextCursor - 커서 페이지네이션의 마지막 데이터 Base64 인코딩 시작");

    String lastId = lastItem.id().toString();
    String lowerSortBy = !(sortBy == null) ? sortBy.toLowerCase() : "participantcount";
    String lastValue;
    switch (lowerSortBy) {
      case "createdat" -> lastValue = lastItem.createdAt().toString();
      case "title" -> lastValue = lastItem.title();
      case "participantcount" -> lastValue = String.valueOf(lastItem.headCount());
      default -> throw new IllegalArgumentException("지원하지 않는 정렬 방식: " + sortBy);
    }
    Cursor cursor = new Cursor(lastValue, lastId);

    return encodeNextCursor(cursor);
  }


  /**
   * 내부에서 핵심 인코딩 로직을 담당합니다.
   *
   * @param cursor dto의 id와 value로 이루어진 커서 객체
   */
  private String encodeNextCursor(Cursor cursor) {
    try {
      // 1. 객체 → JSON 변환
      String cursorToJson = objectMapper.writeValueAsString(cursor);
      // 2. JSON → Base64 문자열 변환 및 반환
      return Base64.getUrlEncoder().encodeToString(cursorToJson.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      log.warn("객체를 Base64로 인코딩 중 오류 발생", e);
      throw new RuntimeException(e);
    }
  }
}
