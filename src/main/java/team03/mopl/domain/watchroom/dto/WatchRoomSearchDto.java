package team03.mopl.domain.watchroom.dto;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WatchRoomSearchDto {

  @Parameter(description = "검색어", example = "밥")
  private String searchKeyword;

  @Parameter(description = "정렬 기준(생성일, 참여자 수, 이름 순)")
  private String sortBy;

  @Parameter(description = "정렬 방향(ASC, DESC)", example = "DESC")
  private String direction;

  @Parameter(description = "커서", example = "")
  private String cursor;

  @Parameter(description = "조회 크기(개수)", example = "20")
  @Max(value = 100)
  @Min(value = 1)
  private Integer size;
}
