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
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("플레이리스트 서비스 테스트")
class PlaylistServiceImplTest {

  @Mock
  private PlaylistRepository playlistRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ContentRepository contentRepository;

  @InjectMocks
  private PlaylistServiceImpl playlistService;

  // 테스트용 유저
  private UUID userId;
  private User user;
  private UUID otherUserId;
  private User otherUser;

  // 테스트용 플레이리스트
  private UUID playlistId;
  private Playlist playlist;

  // 테스트용 콘텐츠
  private UUID contentId1;
  private UUID contentId2;
  private Content content1;
  private Content content2;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    user = User.builder()
        .id(userId)
        .name("테스트유저")
        .email("test@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    otherUserId = UUID.randomUUID();
    otherUser = User.builder()
        .id(otherUserId)
        .name("다른유저")
        .email("other@test.com")
        .password("test")
        .role(Role.USER)
        .build();

    playlistId = UUID.randomUUID();
    playlist = Playlist.builder()
        .id(playlistId)
        .name("테스트 플레이리스트")
        .nameNormalized("테스트플레이리스트")
        .user(user)
        .isPublic(true)
        .playlistContents(new ArrayList<>())
        .build();

    contentId1 = UUID.randomUUID();
    content1 = Content.builder()
        .id(contentId1)
        .title("테스트 콘텐츠 1")
        .description("테스트용 콘텐츠 1입니다.")
        .contentType(ContentType.TV)
        .releaseDate(LocalDateTime.now())
        .build();

    contentId2 = UUID.randomUUID();
    content2 = Content.builder()
        .id(contentId2)
        .title("테스트 콘텐츠 2")
        .description("테스트용 콘텐츠 2입니다.")
        .contentType(ContentType.MOVIE)
        .releaseDate(LocalDateTime.now())
        .build();
  }

  @Nested
  @DisplayName("플레이리스트 생성")
  class CreatePlaylist {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      PlaylistCreateRequest request = new PlaylistCreateRequest("새 플레이리스트", null, true);

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
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();
      PlaylistCreateRequest request = new PlaylistCreateRequest("새 플레이리스트", null, true);

      when(userRepository.findById(randomUserId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(UserNotFoundException.class, () -> playlistService.create(request, randomUserId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }
  }

  @Nested
  @DisplayName("플레이리스트 조회")
  class GetPlaylist {

    @Test
    @DisplayName("ID로 조회 성공")
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
    @DisplayName("존재하지 않는 플레이리스트 조회")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();
      when(playlistRepository.findById(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class, () -> playlistService.getById(randomPlaylistId));
    }

    @Test
    @DisplayName("전체 플레이리스트 조회 성공")
    void getAllSuccess() {
      // given
      List<Playlist> playlists = Arrays.asList(playlist);
      when(playlistRepository.findAll()).thenReturn(playlists);

      // when
      List<PlaylistDto> result = playlistService.getAll();

      // then
      assertNotNull(result);
      assertEquals(1, result.size());
      assertEquals(playlist.getName(), result.get(0).name());
    }

    @Test
    @DisplayName("유저별 플레이리스트 조회 성공")
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
    @DisplayName("존재하지 않는 유저의 플레이리스트 조회")
    void getAllByUserFailsWhenUserNotFound() {
      // given
      UUID randomUserId = UUID.randomUUID();
      when(userRepository.existsById(randomUserId)).thenReturn(false);

      // when & then
      assertThrows(UserNotFoundException.class, () -> playlistService.getAllByUser(randomUserId));
    }

    @Test
    @DisplayName("플레이리스트 검색 성공")
    void searchPlaylistsSuccess() {
      // given
      String keyword = "테스트";
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
    @DisplayName("유저 플레이리스트 조회 - 본인")
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
    @DisplayName("유저 플레이리스트 조회 - 다른 유저")
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
  @DisplayName("플레이리스트 수정")
  class UpdatePlaylist {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      PlaylistUpdateRequest request = new PlaylistUpdateRequest("수정된 플레이리스트", false);
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
      when(playlistRepository.save(any(Playlist.class))).thenReturn(playlist);

      // when
      PlaylistDto result = playlistService.update(playlistId, request, userId);

      // then
      assertNotNull(result);
      verify(playlistRepository, times(1)).save(any(Playlist.class));
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID randomPlaylistId = UUID.randomUUID();
      PlaylistUpdateRequest request = new PlaylistUpdateRequest("수정된 플레이리스트", false);
      when(playlistRepository.findById(randomPlaylistId)).thenReturn(Optional.empty());

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistService.update(randomPlaylistId, request, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("권한이 없는 사용자의 수정 시도")
    void failsWhenNotPlaylistOwner() {
      // given
      PlaylistUpdateRequest request = new PlaylistUpdateRequest("수정된 플레이리스트", false);
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(PlaylistDeniedException.class,
          () -> playlistService.update(playlistId, request, otherUserId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }
  }

  @Nested
  @DisplayName("플레이리스트 삭제")
  class DeletePlaylist {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

      // when
      playlistService.delete(playlistId, userId);

      // then
      verify(playlistRepository, times(1)).deleteById(playlistId);
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
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
    @DisplayName("권한이 없는 사용자의 삭제 시도")
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
  @DisplayName("플레이리스트 콘텐츠 추가")
  class AddContents {

    @Test
    @DisplayName("성공")
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
    @DisplayName("빈 콘텐츠 리스트로 추가 시도")
    void failsWhenContentIdsEmpty() {
      // given
      List<UUID> emptyContentIds = Arrays.asList();

      // when & then
      assertThrows(PlaylistContentEmptyException.class,
          () -> playlistService.addContents(playlistId, emptyContentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("null 콘텐츠 리스트로 추가 시도")
    void failsWhenContentIdsNull() {
      // when & then
      assertThrows(PlaylistContentEmptyException.class,
          () -> playlistService.addContents(playlistId, null, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
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
    @DisplayName("권한이 없는 사용자의 콘텐츠 추가 시도")
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
    @DisplayName("존재하지 않는 콘텐츠들만 있는 경우")
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
    @DisplayName("이미 존재하는 콘텐츠만 추가 시도")
    void failsWhenAllContentsAlreadyExist() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);
      List<Content> contents = Arrays.asList(content1);

      // 이미 플레이리스트에 있는 콘텐츠 설정
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
  @DisplayName("플레이리스트 콘텐츠 삭제")
  class DeleteContents {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);

      // 플레이리스트에 콘텐츠 추가
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
    @DisplayName("빈 콘텐츠 리스트로 삭제 시도")
    void failsWhenContentIdsEmpty() {
      // given
      List<UUID> emptyContentIds = Arrays.asList();

      // when & then
      assertThrows(PlaylistContentRemoveEmptyException.class,
          () -> playlistService.deleteContents(playlistId, emptyContentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("null 콘텐츠 리스트로 삭제 시도")
    void failsWhenContentIdsNull() {
      // when & then
      assertThrows(PlaylistContentRemoveEmptyException.class,
          () -> playlistService.deleteContents(playlistId, null, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
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
    @DisplayName("권한이 없는 사용자의 콘텐츠 삭제 시도")
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
    @DisplayName("삭제할 콘텐츠가 플레이리스트에 없는 경우")
    void failsWhenNoContentsToRemove() {
      // given
      List<UUID> contentIds = Arrays.asList(contentId1);
      // 플레이리스트에 해당 콘텐츠가 없는 상태

      when(playlistRepository.findByIdWithContents(playlistId)).thenReturn(Optional.of(playlist));

      // when & then
      assertThrows(PlaylistContentRemoveEmptyException.class,
          () -> playlistService.deleteContents(playlistId, contentIds, userId));

      verify(playlistRepository, never()).save(any(Playlist.class));
    }
  }
}
