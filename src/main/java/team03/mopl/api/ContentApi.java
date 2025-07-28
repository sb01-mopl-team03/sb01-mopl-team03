package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.content.dto.ContentSearchRequest;
import team03.mopl.domain.review.dto.ReviewDto;

@Tag(name = "Content API", description = "콘텐츠 관련 API")
@RequestMapping("/api/contents")
public interface ContentApi {

  @Operation(summary = "단일 콘텐츠 조회", description = "지정한 ID에 해당하는 콘텐츠 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = ContentDto.class))),
      @ApiResponse(responseCode = "404", description = "콘텐츠를 찾을 수 없음")
  })
  @GetMapping("/{contentId}")
  ResponseEntity<ContentDto> getContent(
      @Parameter(description = "콘텐츠 ID") @PathVariable("contentId") UUID id
  );

  @Operation(summary = "콘텐츠 목록 조회 (커서 기반)", description = "커서 기반 페이지네이션을 이용해 콘텐츠 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = CursorPageResponseDto.class)))
  })
  @GetMapping
  ResponseEntity<CursorPageResponseDto<ContentDto>> getAll(
      @ParameterObject @ModelAttribute ContentSearchRequest contentSearchRequest
  );

  @Operation(summary = "콘텐츠별 리뷰 목록 조회", description = "지정한 콘텐츠에 대한 모든 리뷰를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = ReviewDto.class)))
  })
  @GetMapping("/{contentId}/reviews")
  ResponseEntity<List<ReviewDto>> getAllByContent(
      @Parameter(description = "콘텐츠 ID") @PathVariable("contentId") UUID contentId
  );
}
