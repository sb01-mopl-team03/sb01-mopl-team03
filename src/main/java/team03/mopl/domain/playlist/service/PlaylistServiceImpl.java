package team03.mopl.domain.playlist.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.playlist.PlaylistContentAlreadyExistsException;
import team03.mopl.common.exception.playlist.PlaylistContentEmptyException;
import team03.mopl.common.exception.playlist.PlaylistContentRemoveEmptyException;
import team03.mopl.common.exception.playlist.PlaylistDeniedException;
import team03.mopl.common.exception.playlist.PlaylistNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.common.util.NormalizerUtil;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.notification.events.FollowingPostedPlaylistEvent;
import team03.mopl.domain.notification.events.PlaylistUpdatedEvent;
import team03.mopl.domain.playlist.dto.PlaylistCreateRequest;
import team03.mopl.domain.playlist.dto.PlaylistDto;
import team03.mopl.domain.playlist.dto.PlaylistUpdateRequest;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.entity.PlaylistContent;
import team03.mopl.domain.playlist.repository.PlaylistRepository;
import team03.mopl.domain.subscription.Subscription;
import team03.mopl.domain.subscription.SubscriptionRepository;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PlaylistServiceImpl implements PlaylistService {

  private final PlaylistRepository playlistRepository;
  private final UserRepository userRepository;
  private final ContentRepository contentRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final SubscriptionRepository subscriptionRepository;

  @Override
  @Transactional
  public PlaylistDto create(PlaylistCreateRequest request, UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(UserNotFoundException::new);

    String nameNormalized = NormalizerUtil.normalize(request.name());
    Playlist playlist = Playlist.builder()
        .name(request.name())
        .nameNormalized(nameNormalized)
        .user(user)
        .isPublic(request.isPublic())
        .build();

    Playlist savedPlaylist = playlistRepository.save(playlist);
    log.info("create - 플레이리스트 저장: 플레이리스트 생성자 ID = {}, 플레이리트스 ID = {}", userId, playlist.getId());

    //해당 유저를 구독하고 있던 유저들에게 알림 전송
    eventPublisher.publishEvent(new FollowingPostedPlaylistEvent(
        userId,
        user.getName(),
        savedPlaylist.getId(),
        savedPlaylist.getName(),
        savedPlaylist.isPublic()
    ));

    return PlaylistDto.from(savedPlaylist);
  }

  @Override
  @Transactional
  public PlaylistDto getById(UUID playlistId) {
    Playlist playlist = playlistRepository.findById(playlistId)
        .orElseThrow(PlaylistNotFoundException::new);
    return PlaylistDto.from(playlist);
  }

  @Override
  @Transactional
  public List<PlaylistDto> getAllPublic() {
    List<Playlist> playlists = playlistRepository.findByIsPublicTrue();
    return playlists.stream().map(PlaylistDto::from).toList();
  }

  @Override
  @Transactional
  public List<PlaylistDto> getAllSubscribed(UUID userId) {
    List<Playlist> playlists = subscriptionRepository.findPlaylistsByUserId(userId);
    return playlists.stream().map(PlaylistDto::from).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlaylistDto> getAllByUser(UUID userId) {
    if (!userRepository.existsById(userId)) {
      log.debug("존재하지 않는 유저입니다. 유저 ID = {}", userId);
      throw new UserNotFoundException();
    }

    List<Playlist> playlists = playlistRepository.findAllByUserId(userId);
    return playlists.stream().map(PlaylistDto::from).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlaylistDto> searchPlaylists(String keyword, UUID currentUserId) {
    String normalizedKeyword = NormalizerUtil.normalize(keyword);

    List<Playlist> matchingPlaylists = playlistRepository
        .searchPlaylistsWithNormalizedKeyword(normalizedKeyword, currentUserId);

    return matchingPlaylists.stream()
        .map(PlaylistDto::from)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlaylistDto> getUserPlaylists(UUID targetUserId, UUID currentUserId) {
    log.info("getUserPlaylists - targetUserId: {}, currentUserId: {}", targetUserId, currentUserId);

    List<Playlist> playlists;

    if (targetUserId.equals(currentUserId)) {
      playlists = playlistRepository.findAllByUserId(targetUserId);
    } else {
      playlists = playlistRepository.findPublicPlaylistsByUserId(targetUserId);
    }

    log.info("getUserPlaylists - 조회된 플레이리스트 수: {}", playlists.size());
    return playlists.stream().map(PlaylistDto::from).toList();
  }

  @Override
  public PlaylistDto update(UUID playlistId, PlaylistUpdateRequest request, UUID userId) {
    Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(PlaylistNotFoundException::new);
    if (!playlist.getUser().getId().equals(userId)) {
      log.warn("플레이리스트에 권한이 없습니다. 플레이리스트 생성자 ID = {}", playlist.getUser().getId());
      throw new PlaylistDeniedException();
    }

    playlist.update(request.name(), request.isPublic());

    Playlist savedPlaylist = playlistRepository.save(playlist);
    return PlaylistDto.from(savedPlaylist);
  }

  @Override
  @Transactional
  public void delete(UUID playlistId, UUID userId) {
    Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(PlaylistNotFoundException::new);
    if (!playlist.getUser().getId().equals(userId)) {
      log.warn("플레이리스트에 권한이 없습니다. 플레이리스트 생성자 ID = {}", playlist.getUser().getId());
      throw new PlaylistDeniedException();
    }
    playlistRepository.deleteById(playlistId);
    log.info("delete - 플레이리스트가 삭제되었습니다. 플레이리스트 ID = {}", playlistId);
  }

  @Override
  @Transactional
  public void addContents(UUID playlistId, List<UUID> contentIds, UUID userId) {
    if (contentIds == null || contentIds.isEmpty()) {
      throw new PlaylistContentEmptyException();
    }

    // 2. 플레이리스트 조회 (playlistContents도 함께 fetch)
    Playlist playlist = playlistRepository.findByIdWithContents(playlistId)
        .orElseThrow(PlaylistNotFoundException::new);

    // 3. 권한 확인
    if (!playlist.getUser().getId().equals(userId)) {
      throw new PlaylistDeniedException();
    }

    // 4. 컨텐츠들 존재 확인
    List<Content> contents = contentRepository.findAllById(contentIds);
    if (contents.isEmpty()) {
      throw new PlaylistContentEmptyException();
    }

    // 5. 현재 플레이리스트에 이미 있는 컨텐츠 ID들 조회
    Set<UUID> existingContentIds = playlist.getPlaylistContents().stream()
        .map(pc -> pc.getContent().getId())
        .collect(Collectors.toSet());

    // 6. 중복되지 않은 컨텐츠들만 필터링
    List<Content> newContents = contents.stream()
        .filter(content -> !existingContentIds.contains(content.getId()))
        .toList();

    if (newContents.isEmpty()) {
      throw new PlaylistContentAlreadyExistsException();
    }

    for (Content content : newContents) {
      PlaylistContent playlistContent = PlaylistContent.builder()
          .playlist(playlist)
          .content(content)
          .build();

      playlist.getPlaylistContents().add(playlistContent);
    }

    // 8. Playlist 저장 (cascade로 PlaylistContent도 자동 저장)
    playlistRepository.save(playlist);

    // 9. Playlist를 구독하고 있던 유저에게 알림 전송
    eventPublisher.publishEvent(new PlaylistUpdatedEvent(
        playlist.getId(),
        playlist.getUser().getId(),
        playlist.getName()
    ));
  }

  @Override
  @Transactional
  public void deleteContents(UUID playlistId, List<UUID> contentIds, UUID userId) {
    // 1. 입력 검증
    if (contentIds == null || contentIds.isEmpty()) {
      throw new PlaylistContentRemoveEmptyException();
    }

    // 2. 플레이리스트 조회 (playlistContents도 함께 fetch)
    Playlist playlist = playlistRepository.findByIdWithContents(playlistId)
        .orElseThrow(PlaylistNotFoundException::new);

    // 3. 권한 확인
    if (!playlist.getUser().getId().equals(userId)) {
      throw new PlaylistDeniedException();
    }

    // 4. 제거할 PlaylistContent들 찾기
    Set<UUID> contentIdsToRemove = new HashSet<>(contentIds);
    List<PlaylistContent> contentsToRemove = playlist.getPlaylistContents().stream()
        .filter(pc -> contentIdsToRemove.contains(pc.getContent().getId()))
        .toList();

    if (contentsToRemove.isEmpty()) {
      throw new PlaylistContentRemoveEmptyException();
    }

    // 5. 플레이리스트에서 제거 (orphanRemoval = true로 자동 삭제)
    playlist.getPlaylistContents().removeAll(contentsToRemove);

    // 6. Playlist 저장 (orphanRemoval로 PlaylistContent 자동 삭제)
    playlistRepository.save(playlist);
  }
}
