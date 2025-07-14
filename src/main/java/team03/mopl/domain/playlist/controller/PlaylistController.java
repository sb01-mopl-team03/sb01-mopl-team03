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
public class PlaylistController {

  private final PlaylistService playlistService;

  // 1. 플레이리스트만 생성
  @PostMapping
  public ResponseEntity<PlaylistDto> create(
      @Valid @RequestBody PlaylistCreateRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    return ResponseEntity.ok(playlistService.create(request, userId));
  }

  // 2. 음악 추가/제거 별도 API
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

  @GetMapping("/search")
  public ResponseEntity<List<PlaylistDto>> getPlaylistsByName(
      @RequestParam(required = true) String name) {

    List<PlaylistDto> playlistDtos = playlistService.getAllByName(name);
    return ResponseEntity.ok(playlistDtos);
  }

  @GetMapping
  public ResponseEntity<List<PlaylistDto>> getPlaylistByUser(
      @AuthenticationPrincipal CustomUserDetails userDetails) {

    UUID userId = userDetails.getId();
    List<PlaylistDto> playlistDtos = playlistService.getAllByUser(userId);
    return ResponseEntity.ok(playlistDtos);
  }

  @GetMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> get(@PathVariable UUID playlistId) {

    PlaylistDto playlistDto = playlistService.getById(playlistId);
    return ResponseEntity.ok(playlistDto);
  }

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

  @DeleteMapping("/{playlistId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID playlistId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    UUID userId = userDetails.getId();
    playlistService.delete(playlistId, userId);
    return ResponseEntity.noContent().build();
  }

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
