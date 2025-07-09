package team03.mopl.domain.playlist.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.content.ContentNotFoundException;
import team03.mopl.common.exception.playlist.PlaylistDeleteDeniedException;
import team03.mopl.common.exception.playlist.PlaylistNotFoundException;
import team03.mopl.common.exception.playlist.PlaylistUpdateDeniedException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.common.util.NormalizerUtil;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.playlist.dto.PlaylistCreateRequest;
import team03.mopl.domain.playlist.dto.PlaylistDto;
import team03.mopl.domain.playlist.dto.PlaylistUpdateRequest;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.entity.PlaylistContent;
import team03.mopl.domain.playlist.repository.PlaylistRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

  private final PlaylistRepository playlistRepository;
  private final UserRepository userRepository;
  private final ContentRepository contentRepository;

  @Override
  public PlaylistDto create(PlaylistCreateRequest request) {
    List<Content> contents = contentRepository.findAllById(request.contentIds());
    Set<UUID> existingContentIds = contents.stream()
        .map(Content::getId)
        .collect(Collectors.toSet());

    for (UUID contentId : request.contentIds()) {
      if (!existingContentIds.contains(contentId)) {
        log.debug("존재하지 않는 콘텐츠입니다. 콘텐츠 ID: {}", contentId);
        throw new ContentNotFoundException();
      }
    }

    User user = userRepository.findById(request.userId())
        .orElseThrow(UserNotFoundException::new);

    Playlist playlist = Playlist.builder()
        .name(request.name())
        .user(user)
        .isPublic(request.isPublic())
        .build();

    List<PlaylistContent> playlistContents = new ArrayList<>();
    for (UUID contentId : request.contentIds()) {
      Content content = contents.stream()
          .filter(c -> c.getId().equals(contentId))
          .findFirst()
          .orElseThrow(ContentNotFoundException::new);

      PlaylistContent playlistContent = PlaylistContent.builder()
          .playlist(playlist)
          .content(content)
          .build();
      playlistContents.add(playlistContent);
    }

    playlist.getPlaylistContents().addAll(playlistContents);
    Playlist savedPlaylist = playlistRepository.save(playlist);
    return PlaylistDto.from(savedPlaylist);
  }

  @Override
  public List<PlaylistDto> getAllByUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      log.debug("존재하지 않는 유저입니다. 유저 ID: ", userId);
      throw new UserNotFoundException();
    }

    List<Playlist> playlists = playlistRepository.findAllByUserId(userId);
    return playlists.stream().map(PlaylistDto::from).toList();
  }

  @Override
  public List<PlaylistDto> getAllByName(String name) {
    if (name == null || name.trim().isEmpty()) {
      return Collections.emptyList();
    }

    // 검색어를 정규화
    String normalizedSearchName = NormalizerUtil.normalize(name);

    // 모든 플레이리스트를 가져와서 이름으로 필터링
    List<Playlist> allPlaylists = playlistRepository.findAll();

    List<Playlist> matchingPlaylists = allPlaylists.stream()
        .filter(playlist -> {
          String normalizedPlaylistName = NormalizerUtil.normalize(playlist.getName());
          return normalizedPlaylistName.contains(normalizedSearchName);
        })
        .toList();

    return matchingPlaylists.stream()
        .map(PlaylistDto::from)
        .toList();
  }

  @Override
  public PlaylistDto update(UUID playlistId, PlaylistUpdateRequest request, UUID userId) {
    Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(PlaylistNotFoundException::new);
    if (!playlist.getUser().getId().equals(userId)) {
      log.warn("플레이리스트 생성자만 수정할 수 있습니다. 플레이리스트 생성자 ID: ", playlist.getUser().getId());
      throw new PlaylistUpdateDeniedException();
    }

    playlist.update(request.name(), request.isPublic());

    // 콘텐츠 업데이트
    if (request.contentIds() != null) {
      updatePlaylistContents(playlist, request.contentIds());
    }

    Playlist savedPlaylist = playlistRepository.save(playlist);
    return PlaylistDto.from(savedPlaylist);
  }

  @Override
  public void delete(UUID playlistId, UUID userId) {
    Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(PlaylistNotFoundException::new);
    if (!playlist.getUser().getId().equals(userId)) {
      log.warn("플레이리스트 생성자만 삭제할 수 있습니다. 플레이리스트 생성자 ID: ", playlist.getUser().getId());
      throw new PlaylistDeleteDeniedException();
    }
    playlistRepository.deleteById(playlistId);
  }

  private void updatePlaylistContents(Playlist playlist, List<UUID> newContentIds) {
    // 새로운 콘텐츠들이 실제로 존재하는지 확인
    List<Content> contents = contentRepository.findAllById(newContentIds);
    Set<UUID> existingContentIds = contents.stream()
        .map(Content::getId)
        .collect(Collectors.toSet());

    for (UUID contentId : newContentIds) {
      if (!existingContentIds.contains(contentId)) {
        log.debug("존재하지 않는 콘텐츠입니다. 콘텐츠 ID: {}", contentId);
        throw new ContentNotFoundException();
      }
    }

    // 기존 콘텐츠들 모두 삭제
    playlist.getPlaylistContents().clear();

    // 새로운 콘텐츠들 추가
    Map<UUID, Content> contentMap = contents.stream()
        .collect(Collectors.toMap(Content::getId, content -> content));

    for (UUID contentId : newContentIds) {
      Content content = contentMap.get(contentId);
      PlaylistContent playlistContent = PlaylistContent.builder()
          .playlist(playlist)
          .content(content)
          .build();
      playlist.getPlaylistContents().add(playlistContent);
    }
  }
}
