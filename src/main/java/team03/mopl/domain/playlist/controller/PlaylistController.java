package team03.mopl.domain.playlist.controller;

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

  @PostMapping
  public ResponseEntity<PlaylistDto> create(@RequestBody PlaylistCreateRequest request) {
    return ResponseEntity.ok(playlistService.create(request));
  }

  @GetMapping
  public ResponseEntity<List<PlaylistDto>> getPlaylists(
      @RequestParam(required = false) String name) {
    if (name != null && !name.trim().isEmpty()) {
      List<PlaylistDto> playlistDtos = playlistService.getAllByName(name);
      return ResponseEntity.ok(playlistDtos);
    } else {
      List<PlaylistDto> playlistDtos = playlistService.getAll();
      return ResponseEntity.ok(playlistDtos);
    }
  }

  @PatchMapping("/{playlistId}")
  public ResponseEntity<PlaylistDto> update(
      @PathVariable UUID playlistId,
      @RequestBody PlaylistUpdateRequest request,
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
}
