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
import team03.mopl.domain.playlist.dto.*;
import team03.mopl.jwt.CustomUserDetails;

@Tag(name = "Playlist API", description = "재생목록 관련 API")
@RequestMapping("/api/playlists")
public interface PlaylistApi {

  @Operation(summary = "재생목록 생성")
  @ApiResponse(responseCode = "200", description = "생성된 재생목록 반환", content = @Content(schema = @Schema(implementation = PlaylistDto.class)))
  @PostMapping
  ResponseEntity<PlaylistDto> create(
      @RequestBody PlaylistCreateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "재생목록에 콘텐츠 추가")
  @PostMapping("/{playlistId}/contents")
  ResponseEntity<Void> addContents(
      @PathVariable UUID playlistId,
      @RequestBody AddContentsRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "키워드로 재생목록 검색")
  @GetMapping("/search")
  ResponseEntity<List<PlaylistDto>> getPlaylistsByKeyword(
      @RequestParam String keyword,
      @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "내 재생목록 전체 조회")
  @GetMapping
  ResponseEntity<List<PlaylistDto>> getPlaylistByUser(
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "재생목록 단일 조회")
  @GetMapping("/{playlistId}")
  ResponseEntity<PlaylistDto> get(@PathVariable UUID playlistId);

  @Operation(summary = "재생목록 수정")
  @PatchMapping("/{playlistId}")
  ResponseEntity<PlaylistDto> update(
      @PathVariable UUID playlistId,
      @RequestBody PlaylistUpdateRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "재생목록 삭제")
  @DeleteMapping("/{playlistId}")
  ResponseEntity<Void> delete(
      @PathVariable UUID playlistId,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);

  @Operation(summary = "재생목록 콘텐츠 삭제")
  @DeleteMapping("/{playlistId}/contents")
  ResponseEntity<Void> deleteContents(
      @PathVariable UUID playlistId,
      @RequestBody DeleteContentsRequest request,
      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails);
}
