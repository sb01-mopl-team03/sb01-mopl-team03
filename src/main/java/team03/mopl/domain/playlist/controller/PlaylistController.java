package team03.mopl.domain.playlist.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import team03.mopl.api.PlaylistApi;
import team03.mopl.domain.playlist.dto.AddContentsRequest;
import team03.mopl.domain.playlist.dto.DeleteContentsRequest;
import team03.mopl.domain.playlist.dto.PlaylistCreateRequest;
import team03.mopl.domain.playlist.dto.PlaylistDto;
import team03.mopl.domain.playlist.dto.PlaylistUpdateRequest;
import team03.mopl.domain.playlist.service.PlaylistService;
import team03.mopl.jwt.CustomUserDetails;

@RestController
@RequestMapping("/api/playlists")
@RequiredArgsConstructor
public class PlaylistController implements PlaylistApi {

  private final PlaylistService playlistService;

  // 1. 플레이리스트만 생성
  @Override
  @PostMapping
  public ResponseEntity<PlaylistDto> create(
      @Valid @RequestBody PlaylistCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    return ResponseEntity.ok(playlistService.create(request, userId));
  }

  // 2. 음악 추가/제거 별도 API
  @Override
  @PostMapping("/{playlistId}/contents")
  public ResponseEntity<Void> addContents(
      @PathVariable UUID playlistId,
      @RequestBody AddContentsRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    playlistService.addContents(playlistId, request.contentIds(), userId);
    return ResponseEntity.ok().build();
  }

  @Override
  @GetMapping("/search")
  public ResponseEntity<List<PlaylistDto>> getPlaylistsByKeyword(
      @RequestParam(required = true) String keyword,
      CustomUserDetails userDetails) {

    UUID currentUserId = userDetails.getId();
    List<PlaylistDto> playlistDtos = playlistService.searchPlaylists(keyword, currentUserId);
    return ResponseEntity.ok(playlistDtos);
  }

  @GetMapping
  public ResponseEntity<List<PlaylistDto>> getAllPublic() {
    List<PlaylistDto> playlistDtos = playlistService.getAllPublic();
    return ResponseEntity.ok(playlistDtos);
  }

  @GetMapping("/subscribed")
  public ResponseEntity<List<PlaylistDto>> getAllSubscribed(@AuthenticationPrincipal CustomUserDetails userDetails) {
    UUID userId = userDetails.getId();
    List<PlaylistDto> playlistDtos = playlistService.getAllSubscribed(userId);
    return ResponseEntity.ok(playlistDtos);
  }

  @Override
  @GetMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> get(@PathVariable UUID playlistId) {

    PlaylistDto playlistDto = playlistService.getById(playlistId);
    return ResponseEntity.ok(playlistDto);
  }

  @Override
  @PatchMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> update(
      @PathVariable UUID playlistId,
      @Valid @RequestBody PlaylistUpdateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    PlaylistDto updatedPlaylist = playlistService.update(playlistId, request, userId);
    return ResponseEntity.ok(updatedPlaylist);
  }

  @Override
  @DeleteMapping("/{playlistId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    playlistService.delete(playlistId, userId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @DeleteMapping("/{playlistId}/contents")
  public ResponseEntity<Void> deleteContents(
      @PathVariable UUID playlistId,
      @Valid @RequestBody DeleteContentsRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();
    playlistService.deleteContents(playlistId, request.contentIds(), userId);
    return ResponseEntity.noContent().build();
  }
}
