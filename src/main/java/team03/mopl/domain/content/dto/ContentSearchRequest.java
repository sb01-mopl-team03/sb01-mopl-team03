package team03.mopl.domain.content.dto;

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
 * 모든 필드가 nullable 합니다.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor // @ModelAttribute 가 바인딩할 때 요구하는 생성자
public class ContentSearchRequest {

  private String title;

  @AllowedValues(enumClass = ContentType.class, message = "contentType은 MOVIE, TV, SPORTS 중 하나여야 합니다.")
  private String contentType;

  @AllowedValues(enumClass = SortBy.class, message = "sortBy는 TITLE, RELEASE_AT 중 하나여야 합니다.")
  private String sortBy = "TITLE";

  @AllowedValues(enumClass = SortDirection.class, message = "direction은 DESC, ASC 중 하나여야 합니다.")
  private String direction = "DESC";

  @Pattern(regexp = "^[A-Za-z0-9-_]+={0,2}$", message = "cursor는 Base64 형식 문자열이여야 합니다.")
  private String cursor;

  @Min(value = 1, message = "조회하는 컨텐츠 데이터 개수는 최소 1개 이상이어야 합니다.")
  @Max(value = 50, message = "조회하는 컨텐츠 데이터 개수가 최대 50개를 넘을 수 없습니다.")
  private int size = 20;
}
