package team03.mopl.domain.content.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.review.dto.ReviewResponse;
import team03.mopl.domain.review.service.ReviewService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {

  private final ContentRepository contentRepository;
  private final ReviewService reviewService;
  //
  private final ObjectMapper objectMapper;

  @Override
  public List<ContentDto> getAll() {
    return contentRepository.findAll()
        .stream().map(ContentDto::from)
        .toList();
  }

  /**
   * 컨텐츠 데이터 목록을 커서 기반 페이지네이션으로 조회합니다.
   * <p>
   * 첫 페이지일 경우, 커서값은 null이 됩니다.
   */
  @Override
  public CursorPageResponseDto<ContentDto> getCursorPage(ContentSearchRequest request) {

    // 요청에서 커서 페이지네이션 파라미터 추출
    String title = request.getTitle();
    String contentType = request.getContentType();
    String sortBy = request.getSortBy();
    String direction = request.getDirection();
    String cursor = request.getCursor();
    int size = request.getSize();

    log.info(
        "getCursorPage - 컨텐츠 데이터 목록 조회 시작: title={}, contentType={}, sort={}, direction={}, cursor={}, size={}",
        title, contentType, sortBy, direction, cursor, size);

    // 1. cursor 값 디코딩
    // cursor이 존재할 때만 디코딩하여 값을 할당합니다.
    Cursor decodeCursor;
    String mainCursorValue = null;
    UUID subCursorId = null;
    if (cursor != null && !cursor.isEmpty()) {
      decodeCursor = decodeCursor(cursor);
      mainCursorValue = decodeCursor.lastValue();
      subCursorId = UUID.fromString(decodeCursor.lastId());
    }

    // 2. 커서 페이지네이션 적용된 컨텐츠 데이터 조회
    List<Content> contents =
        contentRepository.findContentsWithCursor(
            title,
            contentType,
            sortBy,
            direction,
            mainCursorValue,
            subCursorId,
            size + 1 // + 1 다음 페이지 존재 여부 확인
        );

    // 3. 다음 페이지 존재 여부 판단 및 실제 리스트 제거
    boolean hasNext = contents.size() > size;
    if (hasNext) {
      log.debug("+1 제거 전 contents의 size 확인: contents.size={}", contents.size());
      contents = contents.subList(0, size);
      log.debug("+1 제거 후 contents의 size 확인: contents.size={}", contents.size());
    }

    // 4. 총 데이터 개수 반환
    long totalElements = contents.size();

    // 5. Content를 ContentDto로 변환
    List<ContentDto> contentDtos = contents.stream()
        .map(ContentDto::from)
        .toList();

    // 6. 다음 페이지의 커서 지정 및 인코딩
    String nextCursor = null;
    if (hasNext) {
      nextCursor = encodeNextCursor(contentDtos.get(contentDtos.size() - 1), sortBy);
    }

    log.info("getCursorPage - 컨텐츠 데이터 목록 조회 끝");
    return
        CursorPageResponseDto.<ContentDto>builder()
            .data(contentDtos)
            .nextCursor(nextCursor)
            .size(size)
            .totalElements(totalElements)
            .hasNext(hasNext)
            .build();
  }

  @Override
  public ContentDto getContent(UUID id) {
    // 컨텐츠 존재 유무 검증
    Content content = contentRepository.findById(id)
        .orElseThrow(ContentNotFoundException::new);
    return ContentDto.from(content);
  }

  @Override
  public void updateContentRating(UUID contentId) {
    BigDecimal averageRating = calculateRating(contentId);

    Content content = contentRepository.findById(contentId)
        .orElseThrow(ContentNotFoundException::new);

    content.setAvgRating(averageRating);
    contentRepository.save(content);
  }

  // review 평점의 평균값 게산
  private BigDecimal calculateRating(UUID contentId) {
    List<ReviewResponse> reviews = reviewService.findAllByContent(contentId);
    if (reviews == null || reviews.isEmpty()) {
      return BigDecimal.ZERO;
    }

    BigDecimal sum = reviews.stream()
        .map(ReviewResponse::rating)
        .filter(Objects::nonNull) // null 값 제외
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return sum.divide(new BigDecimal(reviews.size()), 2, RoundingMode.HALF_UP);
  }

  /**
   * 인코딩된 cursor 값을 디코딩합니다.
   *
   * @param encodedCursor 인코딩된 문자열 값
   */
  private Cursor decodeCursor(String encodedCursor) {
    log.info("decodeCursor - cursor 값 Base64 디코딩 시작");

    try {
      // 1. Base64 문자열 → Byte 변환
      byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedCursor);
      // 2. Byte → JSON 변환
      String decodedJson = new String(decodedBytes, StandardCharsets.UTF_8);
      // 4. JSON → Cursor 객체 변환및 반환
      return objectMapper.readValue(decodedJson, Cursor.class);
    } catch (Exception e) {
      log.warn("Base64 문자열을 디코딩하여 객체로 변환 중 오류 발생", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * 커서 페이지네이션의 마지막 데이터를 인코딩하여 반환합니다.
   *
   * @param contentDto 커서 페이지네이션의 마지막 데이터
   */
  private String encodeNextCursor(ContentDto contentDto, String sortBy) {
    log.info("encodeNextCursor - 커서 페이지네이션의 마지막 데이터 Base64 인코딩 시작");

    String lastId = contentDto.id().toString();
    String lastValue;
    if (sortBy.equalsIgnoreCase("RELEASE_AT")) {
      lastValue = contentDto.releaseDate().toString();
    } else {
      lastValue = contentDto.titleNormalized();
    }

    Cursor cursor = new Cursor(lastId, lastValue);
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
