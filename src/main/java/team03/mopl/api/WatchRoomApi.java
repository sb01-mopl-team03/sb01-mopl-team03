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
import org.springframework.web.bind.annotation.*;

import team03.mopl.domain.watchroom.dto.WatchRoomCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomDto;

@Tag(name = "Watch Room API", description = "같이보기 채팅방 관련 API")
@RequestMapping("/api/rooms")
public interface WatchRoomApi {

  @Operation(summary = "전체 채팅방 목록 조회", description = "모든 같이보기 채팅방 목록을 조회합니다.")
  @ApiResponse(responseCode = "200", description = "조회 성공",
      content = @Content(schema = @Schema(implementation = WatchRoomDto.class)))
  @GetMapping
  ResponseEntity<List<WatchRoomDto>> getChatRooms();

  @Operation(summary = "채팅방 단건 조회", description = "채팅방 ID로 채팅방 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = WatchRoomDto.class))),
      @ApiResponse(responseCode = "404", description = "채팅방을 찾을 수 없음")
  })
  @GetMapping("/{roomId}")
  ResponseEntity<WatchRoomDto> getChatRoom(
      @Parameter(description = "채팅방 ID") @PathVariable("roomId") String roomId);

  @Operation(summary = "채팅방 생성", description = "새로운 같이보기 채팅방을 생성합니다.")
  @ApiResponse(responseCode = "201", description = "생성 성공",
      content = @Content(schema = @Schema(implementation = WatchRoomDto.class)))
  @PostMapping
  ResponseEntity<WatchRoomDto> createChatRoom(@RequestBody WatchRoomCreateRequest request);
}
