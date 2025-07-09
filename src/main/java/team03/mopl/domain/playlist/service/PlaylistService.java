package team03.mopl.domain.playlist.service;

import java.util.List;
import java.util.UUID;
import team03.mopl.domain.playlist.dto.PlaylistCreateRequest;
import team03.mopl.domain.playlist.dto.PlaylistDto;
import team03.mopl.domain.playlist.dto.PlaylistUpdateRequest;

public interface PlaylistService {

  PlaylistDto create(PlaylistCreateRequest request);

  List<PlaylistDto> getAllByUser(UUID userId);

  List<PlaylistDto> getAllByName(String name);

  List<PlaylistDto> getAll();

  PlaylistDto update(UUID playlistId, PlaylistUpdateRequest request, UUID userId);

  void delete(UUID playlistId, UUID userId);
}
