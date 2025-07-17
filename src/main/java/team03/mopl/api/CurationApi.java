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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import team03.mopl.domain.content.dto.ContentDto;
import team03.mopl.domain.curation.dto.KeywordDto;
import team03.mopl.domain.curation.dto.KeywordRequest;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "Curation API", description = "추천 키워드 기반 콘텐츠 큐레이션 API")
@RequestMapping("/api/keywords")
public interface CurationApi {

  @Operation(summary = "키워드 등록", description = "사용자가 관심 키워드를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "등록 성공", content = @Content(schema = @Schema(implementation = KeywordDto.class))),
      @ApiResponse(responseCode = "400", description = "요청이 유효하지 않음"),
      @ApiResponse(responseCode = "500", description = "서버 오류")
  })
  @PostMapping
  ResponseEntity<KeywordDto> registerKeyword(@RequestBody KeywordRequest request);

  @Operation(summary = "키워드 삭제", description = "사용자가 등록한 키워드를 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "해당 키워드 없음")
  })
  @DeleteMapping("/{keywordId}")
  ResponseEntity<Void> delete(@PathVariable UUID keywordId, @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
