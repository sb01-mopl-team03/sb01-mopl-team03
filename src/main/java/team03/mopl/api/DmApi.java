package team03.mopl.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.RequestMapping;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmPagingDto;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "DM API", description = "DM(Direct Message) 관련 API")
@RequestMapping("/api/dm")
public interface DmApi {

  @Operation(summary = "DM 메시지 조회", description = "DM 방 ID에 해당하는 메시지 목록을 커서 기반으로 조회합니다. 요청 시 읽음 처리도 함께 수행됩니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = CursorPageResponseDto.class))),
      @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "DM 방 또는 메시지 없음")
  })
  @GetMapping("/{roomId}")
  ResponseEntity<CursorPageResponseDto<DmDto>> getDm(
      @Parameter(description = "DM 방 ID") @PathVariable("roomId") UUID roomId,
      @ParameterObject @ModelAttribute DmPagingDto dmPagingDto,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  );
}
