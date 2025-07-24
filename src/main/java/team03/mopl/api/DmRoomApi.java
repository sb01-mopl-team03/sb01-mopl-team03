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
import org.springframework.web.bind.annotation.*;

import team03.mopl.domain.dm.dto.DmRoomDto;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "DM Room API", description = "DM 방 관련 API")
@RequestMapping("/api/dmRooms")
public interface DmRoomApi {

  @Operation(summary = "DM 방 조회", description = "DM 방 ID를 기반으로 DM 방 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = DmRoomDto.class)))
  @GetMapping("/{dmRoomId}")
  ResponseEntity<DmRoomDto> getRoom(@PathVariable(name = "dmRoomId") UUID dmRoomId);

  @Operation(summary = "사용자 간 DM 방 조회 또는 생성", description = "현재 사용자와 상대 사용자 간 DM 방이 존재하면 조회하고, 없으면 생성하여 반환합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 또는 생성 성공", content = @Content(schema = @Schema(implementation = UUID.class)))
  })
  @GetMapping("/userRoom")
  ResponseEntity<UUID> getOrCreateRoom(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestParam(name = "userB") UUID userB
  );

  @Operation(summary = "현재 사용자의 모든 DM 방 조회", description = "로그인한 사용자가 참여 중인 모든 DM 방을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공")
  @GetMapping("/")
  ResponseEntity<List<DmRoomDto>> getAllRooms(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "DM 방 삭제", description = "현재 사용자가 참여 중인 DM 방을 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "해당 DM 방 없음")
  })
  @DeleteMapping("/{roomId}")
  ResponseEntity<Void> deleteRoom(
      @PathVariable(name = "roomId") UUID roomId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  );

  @Operation(
      summary = "DM 방 재입장",
      description = "나갔던(out) DM 방에 다시 참여합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "재입장 성공"),
      @ApiResponse(responseCode = "403", description = "권한 없음"),
      @ApiResponse(responseCode = "404", description = "DM 방 없음")
  })
  @PatchMapping("/{roomId}/reenter")
  ResponseEntity<Void> reenterRoom(
      @PathVariable("roomId") UUID roomId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
  );
}
