package team03.mopl.domain.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.SortBy;
import team03.mopl.domain.content.SortDirection;
import team03.mopl.domain.content.validation.AllowedValues;

/**
 * 콘텐츠 검색 요청 DTO (모든 필드 nullable)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "콘텐츠 검색 요청 파라미터")
public class ContentSearchRequest {

  @Schema(description = "제목 검색 키워드", example = "센과 치히로")
  private String title;

  @Schema(description = "콘텐츠 타입 (MOVIE, TV, SPORTS)", example = "MOVIE", defaultValue = "MOVIE")
  @AllowedValues(enumClass = ContentType.class, message = "contentType은 MOVIE, TV, SPORTS 중 하나여야 합니다.")
  private String contentType = "MOVIE";

  @Schema(description = "정렬 기준 (TITLE, RELEASE_AT)", example = "TITLE", defaultValue = "TITLE")
  @AllowedValues(enumClass = SortBy.class, message = "sortBy는 TITLE, RELEASE_AT, AVG_RATING 중 하나여야 합니다.")
  private String sortBy = "TITLE";

  @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC", defaultValue = "DESC")
  @AllowedValues(enumClass = SortDirection.class, message = "direction은 DESC, ASC 중 하나여야 합니다.")
  private String direction = "DESC";

  @Schema(description = "커서 기반 페이지네이션 커서(Base64 형식)", example = "Y3JlYXRlZEF0OjIwMjUtMDctMTZUMTA6MDA6MDA=")
  @Pattern(regexp = "^[A-Za-z0-9-_]+={0,2}$", message = "cursor는 Base64 형식 문자열이여야 합니다.")
  private String cursor;

  @Schema(description = "조회할 콘텐츠 개수 (1~50)", example = "20", defaultValue = "20")
  @Min(value = 1, message = "조회하는 컨텐츠 데이터 개수는 최소 1개 이상이어야 합니다.")
  @Max(value = 50, message = "조회하는 컨텐츠 데이터 개수가 최대 50개를 넘을 수 없습니다.")
  private int size = 20;
}
