package team03.mopl.domain.playlist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import team03.mopl.common.exception.playlist.PlaylistContentAlreadyExistsException;
import team03.mopl.common.exception.playlist.PlaylistContentEmptyException;
import team03.mopl.common.exception.playlist.PlaylistContentRemoveEmptyException;
import team03.mopl.common.exception.playlist.PlaylistDeniedException;
import team03.mopl.common.exception.playlist.PlaylistNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.content.Content;
import team03.mopl.domain.content.ContentType;
import team03.mopl.domain.content.repository.ContentRepository;
import team03.mopl.domain.playlist.dto.PlaylistCreateRequest;
import team03.mopl.domain.playlist.dto.PlaylistDto;
import team03.mopl.domain.playlist.dto.PlaylistUpdateRequest;
import team03.mopl.domain.playlist.entity.Playlist;
import team03.mopl.domain.playlist.entity.PlaylistContent;
import team03.mopl.domain.playlist.repository.PlaylistRepository;
import team03.mopl.domain.playlist.service.PlaylistServiceImpl;
import team03.mopl.domain.subscription.SubscriptionRepository;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Playlist Service Test")
class PlaylistServiceImplTest {

  @Mock
  private PlaylistRepository playlistRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ContentRepository contentRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @InjectMocks
  private PlaylistServiceImpl playlistService;

  // 테스트용 유저
  private UUID userId;
  private User user;
  private UUID otherUserId;
  private User otherUser;

  // Test playlist
  private UUID playlistId;
  private Playlist playlist;

  // Test contents
  private UUID contentId1;
  private UUID contentId2;
  private Content content1;
  private Content content2;

  @BeforeEach
  void setUp() {
    // Initialize Mock objects
    reset(eventPublisher);

    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .name("Test User")
        .email("test@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    otherUserId = UUID.randomUUID();
    otherUser = User.builder()
        .id(otherUserId)
        .name("Other User")
        .email("other@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    playlistId = UUID.randomUUID();
    playlist = Playlist.builder()
        .id(playlistId)
        .name("Test Playlist")
        .nameNormalized("testplaylist")
        .user(user)
        .isPublic(true)
        .playlistContents(new ArrayList<>())
        .build();

    contentId1 = UUID.randomUUID();
    content1 = Content.builder()
        .id(contentId1)
        .title("Test Content 1")
        .description("Test content 1 description")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .build();

    contentId2 = UUID.randomUUID();
    content2 = Content.builder()
        .id(contentId2)
        .title("Test Content 2")
        .description("Test content 2 description")
        .contentType(ContentType.MOVIE)
        .releaseDate(LocalDateTime.now())
        .build();
  }

  @Nested
  @DisplayName("Create Playlist")
  class CreatePlaylist {

    @Test
    @DisplayName("Success")
    void success() {
      // given
      PlaylistCreateRequest request = new PlaylistCreateRequest("New Playlist", null, true);

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

      // when
      PlaylistDto result = playlistService.create(request, userId);

      // then
      assertNotNull(result);
      assertEquals(playlist.getName(), result.name());
      assertEquals(playlist.isPublic(), result.isPublic());

      verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when user not found")
    void failsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();
      PlaylistCreateRequest request = new PlaylistCreateRequest("New Playlist", null, true);

      when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class, () -> playlistService.create(request, randomUserId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }
  }

  @Nested
  @DisplayName("Get Playlist")
  class GetPlaylist {

    @Test
    @DisplayName("Get by ID success")
    void getByIdSuccess() {
      // given
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

      // when
      PlaylistDto result = playlistService.getById(playlistId);

      // then
      assertNotNull(result);
      assertEquals(playlist.getName(), result.name());
      assertEquals(playlist.isPublic(), result.isPublic());
    }

    @Test
    @DisplayName("Fails when playlist not found")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();
      when(playlistRepository.findById(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class, () -> playlistService.getById(randomPlaylistId));
    }

    @Test
    @DisplayName("Get all public playlists success")
    void getAllPublicSuccess() {
      // given
      List<Playlist> playlists = Arrays.asList(playlist);
      when(playlistRepository.findByIsPublicTrue()).thenReturn(playlists);

      // when
      List<PlaylistDto> result = playlistService.getAllPublic();

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(playlist.getName(), result.get(0).name());
    }

    @Test
    @DisplayName("Get all subscribed playlists success")
    void getAllSubscribedSuccess() {
      // given
      List<Playlist> playlists = Arrays.asList(playlist);
      when(subscriptionRepository.findPlaylistsByUserId(userId)).thenReturn(playlists);

      // when
      List<PlaylistDto> result = playlistService.getAllSubscribed(userId);

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(playlist.getName(), result.get(0).name());
    }

    @Test
    @DisplayName("Get all playlists by user success")
    void getAllByUserSuccess() {
      // given
      List<Playlist> playlists = Arrays.asList(playlist);
      when(userRepository.existsById(userId)).thenReturn(true);
      when(playlistRepository.findAllByUserId(userId)).thenReturn(playlists);

      // when
      List<PlaylistDto> result = playlistService.getAllByUser(userId);

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(playlist.getName(), result.get(0).name());
    }

    @Test
    @DisplayName("Get all by user fails when user not found")
    void getAllByUserFailsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();
      when(userRepository.existsById(randomUserId)).thenReturn(false);

      // when & then
      assertThrows(UserNotFoundException.class, () -> playlistService.getAllByUser(randomUserId));
    }

    @Test
    @DisplayName("Search playlists success")
    void searchPlaylistsSuccess() {
      // given
      String keyword = "test";
      List<Playlist> playlists = Arrays.asList(playlist);
      when(playlistRepository.searchPlaylistsWithNormalizedKeyword(any(String.class), eq(userId)))
          .thenReturn(playlists);

      // when
      List<PlaylistDto> result = playlistService.searchPlaylists(keyword, userId);

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(playlist.getName(), result.get(0).name());
    }

    @Test
    @DisplayName("Get user playlists for self success")
    void getUserPlaylistsForSelfSuccess() {
      // given
      List<Playlist> playlists = Arrays.asList(playlist);
      when(playlistRepository.findAllByUserId(userId)).thenReturn(playlists);

      // when
      List<PlaylistDto> result = playlistService.getUserPlaylists(userId, userId);

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      verify(playlistRepository).findAllByUserId(userId);
      verify(playlistRepository, never()).findPublicPlaylistsByUserId(any());
    }

    @Test
    @DisplayName("Get user playlists for other success")
    void getUserPlaylistsForOtherSuccess() {
      // given
      List<Playlist> publicPlaylists = Arrays.asList(playlist);
      when(playlistRepository.findPublicPlaylistsByUserId(otherUserId)).thenReturn(publicPlaylists);

      // when
      List<PlaylistDto> result = playlistService.getUserPlaylists(otherUserId, userId);

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      verify(playlistRepository).findPublicPlaylistsByUserId(otherUserId);
      verify(playlistRepository, never()).findAllByUserId(otherUserId);
    }
  }

  @Nested
  @DisplayName("Update Playlist")
  class UpdatePlaylist {

    @Test
    @DisplayName("Success")
    void success() {
      // given
      PlaylistUpdateRequest request = new PlaylistUpdateRequest("Updated Playlist", false);
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
      when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

      // when
      PlaylistDto result = playlistService.update(playlistId, request, userId);

      // then
      assertNotNull(result);
      verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when playlist not found")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();
      PlaylistUpdateRequest request = new PlaylistUpdateRequest("Updated Playlist", false);
      when(playlistRepository.findById(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistService.update(randomPlaylistId, request, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when not playlist owner")
    void failsWhenNotPlaylistOwner() {
      // given
      PlaylistUpdateRequest request = new PlaylistUpdateRequest("Updated Playlist", false);
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(PlaylistDeniedException.class,
          () -> playlistService.update(playlistId, request, otherUserId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }
  }

  @Nested
  @DisplayName("Delete Playlist")
  class DeletePlaylist {

    @Test
    @DisplayName("Success")
    void success() {
      // given
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

      // when
      playlistService.delete(playlistId, userId);

      // then
      verify(playlistRepository, times(1)).deleteById(playlistId);
    }

    @Test
    @DisplayName("Fails when playlist not found")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();
      when(playlistRepository.findById(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistService.delete(randomPlaylistId, userId));

      verify(playlistRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Fails when not playlist owner")
    void failsWhenNotPlaylistOwner() {
      // given
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(PlaylistDeniedException.class,
          () -> playlistService.delete(playlistId, otherUserId));

      verify(playlistRepository, never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("Add Contents")
  class AddContents {

    @Test
    @DisplayName("Success")
    void success() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1, contentId2);
      List<Content> contents = Arrays.asList(content1, content2);

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));
      when(contentRepository.findAllById(contentIds)).thenReturn(contents);
      when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

      // when
      playlistService.addContents(playlistId, contentIds, userId);

      // then
      verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when content IDs empty")
    void failsWhenContentIdsEmpty() {
      // given
      List<UUID> emptyContentIds = Arrays.asList();

      // when & then
      assertThrows(PlaylistContentEmptyException.class,
          () -> playlistService.addContents(playlistId, emptyContentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when content IDs null")
    void failsWhenContentIdsNull() {
      // when & then
      assertThrows(PlaylistContentEmptyException.class,
          () -> playlistService.addContents(playlistId, null, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when playlist not found")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();
      List<UUID> contentIds = Arrays.asList(contentId1);

      when(playlistRepository.findByIdWithContents(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistService.addContents(randomPlaylistId, contentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when not playlist owner")
    void failsWhenNotPlaylistOwner() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(PlaylistDeniedException.class,
          () -> playlistService.addContents(playlistId, contentIds, otherUserId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when no valid contents")
    void failsWhenNoValidContents() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);
      List<Content> emptyContents = Arrays.asList();

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));
      when(contentRepository.findAllById(contentIds)).thenReturn(emptyContents);

      // when & then
      assertThrows(PlaylistContentEmptyException.class,
          () -> playlistService.addContents(playlistId, contentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when all contents already exist")
    void failsWhenAllContentsAlreadyExist() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);
      List<Content> contents = Arrays.asList(content1);

      // Add existing content to playlist
      PlaylistContent existingPlaylistContent = PlaylistContent.builder()
          .playlist(playlist)
          .content(content1)
          .build();
      playlist.getPlaylistContents().add(existingPlaylistContent);

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));
      when(contentRepository.findAllById(contentIds)).thenReturn(contents);

      // when & then
      assertThrows(PlaylistContentAlreadyExistsException.class,
          () -> playlistService.addContents(playlistId, contentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }
  }

  @Nested
  @DisplayName("Delete Contents")
  class DeleteContents {

    @Test
    @DisplayName("Success")
    void success() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);

      // Add content to playlist
      PlaylistContent playlistContent = PlaylistContent.builder()
          .playlist(playlist)
          .content(content1)
          .build();
      playlist.getPlaylistContents().add(playlistContent);

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));
      when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

      // when
      playlistService.deleteContents(playlistId, contentIds, userId);

      // then
      verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when content IDs empty")
    void failsWhenContentIdsEmpty() {
      // given
      List<UUID> emptyContentIds = Arrays.asList();

      // when & then
      assertThrows(PlaylistContentRemoveEmptyException.class,
          () -> playlistService.deleteContents(playlistId, emptyContentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when content IDs null")
    void failsWhenContentIdsNull() {
      // when & then
      assertThrows(PlaylistContentRemoveEmptyException.class,
          () -> playlistService.deleteContents(playlistId, null, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when playlist not found")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();
      List<UUID> contentIds = Arrays.asList(contentId1);

      when(playlistRepository.findByIdWithContents(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistService.deleteContents(randomPlaylistId, contentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when not playlist owner")
    void failsWhenNotPlaylistOwner() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(PlaylistDeniedException.class,
          () -> playlistService.deleteContents(playlistId, contentIds, otherUserId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("Fails when no contents to remove")
    void failsWhenNoContentsToRemove() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);
      // Playlist has no such content

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(PlaylistContentRemoveEmptyException.class,
          () -> playlistService.deleteContents(playlistId, contentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }
  }
}
