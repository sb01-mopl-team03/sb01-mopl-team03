package team03.mopl.domain.playlist;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import team03.mopl.common.exception.playlist.PlaylistNotFoundException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.playlist.controller.PlaylistController;
import team03.mopl.domain.playlist.dto.AddContentsRequest;
import team03.mopl.domain.playlist.dto.DeleteContentsRequest;
import team03.mopl.domain.playlist.dto.PlaylistCreateRequest;
import team03.mopl.domain.playlist.dto.PlaylistDto;
import team03.mopl.domain.playlist.dto.PlaylistUpdateRequest;
import team03.mopl.domain.playlist.service.PlaylistService;
import team03.mopl.jwt.CustomUserDetails;

@ExtendWith(MockitoExtension.class)
@DisplayName("플레이리스트 컨트롤러 테스트")
class PlaylistControllerTest {

  @Mock
  private PlaylistService playlistService;

  @Mock
  private CustomUserDetails userDetails;

  @InjectMocks
  private PlaylistController playlistController;

  @Nested
  @DisplayName("플레이리스트 생성 요청")
  class CreatePlaylist {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      LocalDateTime createdAt = LocalDateTime.now();

      PlaylistCreateRequest request = new PlaylistCreateRequest("테스트 플레이리스트", "테스트 설명", true);

      PlaylistDto mockResponse = new PlaylistDto(playlistId, "테스트 플레이리스트",
          userId, "테스트유저", true, createdAt, List.of(), List.of());

      when(userDetails.getId()).thenReturn(userId);
      when(playlistService.create(request, userId)).thenReturn(mockResponse);

      // when
      ResponseEntity<PlaylistDto> response = playlistController.create(request, userDetails);

      // then
      assertNotNull(response.getBody());
      assertEquals(mockResponse.name(), response.getBody().name());
      assertEquals(mockResponse.isPublic(), response.getBody().isPublic());
      assertEquals(mockResponse.userId(), response.getBody().userId());
      verify(playlistService).create(request, userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      UUID userId = UUID.randomUUID();
      PlaylistCreateRequest request = new PlaylistCreateRequest("테스트 플레이리스트", "테스트 설명", true);

      when(userDetails.getId()).thenReturn(userId);
      when(playlistService.create(request, userId)).thenThrow(new UserNotFoundException());

      // when & then
      assertThrows(UserNotFoundException.class, () -> playlistController.create(request, userDetails));
    }
  }

  @Nested
  @DisplayName("플레이리스트 컨텐츠 추가 요청")
  class AddContents {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      List<UUID> contentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

      AddContentsRequest request = new AddContentsRequest(contentIds);

      when(userDetails.getId()).thenReturn(userId);

      // when
      ResponseEntity<Void> response = playlistController.addContents(playlistId, request, userDetails);

      // then
      assertEquals(200, response.getStatusCodeValue());
      assertNull(response.getBody());
      verify(playlistService).addContents(playlistId, contentIds, userId);
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      List<UUID> contentIds = List.of(UUID.randomUUID());

      AddContentsRequest request = new AddContentsRequest(contentIds);

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new PlaylistNotFoundException()).when(playlistService).addContents(playlistId, contentIds, userId);

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistController.addContents(playlistId, request, userDetails));
    }

    @Test
    @DisplayName("다른 사용자의 플레이리스트에 컨텐츠 추가 시도")
    void failsWhenAccessDenied() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      List<UUID> contentIds = List.of(UUID.randomUUID());

      AddContentsRequest request = new AddContentsRequest(contentIds);

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new AccessDeniedException("본인의 플레이리스트만 수정할 수 있습니다"))
          .when(playlistService).addContents(playlistId, contentIds, userId);

      // when & then
      assertThrows(AccessDeniedException.class,
          () -> playlistController.addContents(playlistId, request, userDetails));
    }
  }

  @Nested
  @DisplayName("키워드로 플레이리스트 검색 요청")
  class GetPlaylistsByKeyword {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      String keyword = "테스트";
      UUID userId = UUID.randomUUID();
      UUID playlistId = UUID.randomUUID();
      LocalDateTime createdAt = LocalDateTime.now();

      PlaylistDto mockPlaylist = new PlaylistDto(playlistId, "테스트 플레이리스트",
          userId, "테스트유저", true, createdAt, List.of(), List.of());
      List<PlaylistDto> mockResponse = List.of(mockPlaylist);

      when(userDetails.getId()).thenReturn(userId);
      when(playlistService.searchPlaylists(keyword, userId)).thenReturn(mockResponse);

      // when
      ResponseEntity<List<PlaylistDto>> response = playlistController.getPlaylistsByKeyword(keyword, userDetails);

      // then
      assertNotNull(response.getBody());
      assertEquals(1, response.getBody().size());
      assertEquals(mockPlaylist.name(), response.getBody().get(0).name());
      verify(playlistService).searchPlaylists(keyword, userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저")
    void failsWhenUserNotFound() {
      // given
      String keyword = "테스트";
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);
      when(playlistService.searchPlaylists(keyword, userId)).thenThrow(new UserNotFoundException());

      // when & then
      assertThrows(UserNotFoundException.class,
          () -> playlistController.getPlaylistsByKeyword(keyword, userDetails));
    }
  }

  @Nested
  @DisplayName("플레이리스트 단건 조회 요청")
  class GetPlaylist {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      LocalDateTime createdAt = LocalDateTime.now();

      PlaylistDto mockResponse = new PlaylistDto(playlistId, "테스트 플레이리스트",
          userId, "테스트유저", true, createdAt, List.of(), List.of());

      when(playlistService.getById(playlistId)).thenReturn(mockResponse);

      // when
      ResponseEntity<PlaylistDto> response = playlistController.get(playlistId);

      // then
      assertNotNull(response.getBody());
      assertEquals(mockResponse.id(), response.getBody().id());
      assertEquals(mockResponse.name(), response.getBody().name());
      assertEquals(mockResponse.isPublic(), response.getBody().isPublic());
      verify(playlistService).getById(playlistId);
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID playlistId = UUID.randomUUID();

      when(playlistService.getById(playlistId)).thenThrow(new PlaylistNotFoundException());

      // when & then
      assertThrows(PlaylistNotFoundException.class, () -> playlistController.get(playlistId));
    }
  }

  @Nested
  @DisplayName("플레이리스트 수정 요청")
  class UpdatePlaylist {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      LocalDateTime createdAt = LocalDateTime.now();

      PlaylistUpdateRequest request = new PlaylistUpdateRequest("수정된 플레이리스트", false);

      PlaylistDto mockResponse = new PlaylistDto(playlistId, "수정된 플레이리스트",
          userId, "테스트유저", false, createdAt, List.of(), List.of());

      when(userDetails.getId()).thenReturn(userId);
      when(playlistService.update(playlistId, request, userId)).thenReturn(mockResponse);

      // when
      ResponseEntity<PlaylistDto> response = playlistController.update(playlistId, request, userDetails);

      // then
      assertNotNull(response.getBody());
      assertEquals("수정된 플레이리스트", response.getBody().name());
      assertEquals(false, response.getBody().isPublic());
      verify(playlistService).update(playlistId, request, userId);
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      PlaylistUpdateRequest request = new PlaylistUpdateRequest("수정된 플레이리스트", false);

      when(userDetails.getId()).thenReturn(userId);
      when(playlistService.update(playlistId, request, userId)).thenThrow(new PlaylistNotFoundException());

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistController.update(playlistId, request, userDetails));
    }

    @Test
    @DisplayName("다른 사용자의 플레이리스트 수정 시도")
    void failsWhenAccessDenied() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      PlaylistUpdateRequest request = new PlaylistUpdateRequest("수정된 플레이리스트", false);

      when(userDetails.getId()).thenReturn(userId);
      when(playlistService.update(playlistId, request, userId))
          .thenThrow(new AccessDeniedException("본인의 플레이리스트만 수정할 수 있습니다"));

      // when & then
      assertThrows(AccessDeniedException.class,
          () -> playlistController.update(playlistId, request, userDetails));
    }
  }

  @Nested
  @DisplayName("플레이리스트 삭제 요청")
  class DeletePlaylist {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);

      // when
      ResponseEntity<Void> response = playlistController.delete(playlistId, userDetails);

      // then
      assertEquals(204, response.getStatusCodeValue()); // NO_CONTENT
      assertNull(response.getBody());
      verify(playlistService).delete(playlistId, userId);
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new PlaylistNotFoundException()).when(playlistService).delete(playlistId, userId);

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistController.delete(playlistId, userDetails));
    }

    @Test
    @DisplayName("다른 사용자의 플레이리스트 삭제 시도")
    void failsWhenAccessDenied() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new AccessDeniedException("본인의 플레이리스트만 삭제할 수 있습니다"))
          .when(playlistService).delete(playlistId, userId);

      // when & then
      assertThrows(AccessDeniedException.class,
          () -> playlistController.delete(playlistId, userDetails));
    }
  }

  @Nested
  @DisplayName("플레이리스트 컨텐츠 삭제 요청")
  class DeleteContents {

    @Test
    @DisplayName("성공")
    void success() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      List<UUID> contentIds = List.of(UUID.randomUUID(), UUID.randomUUID());

      DeleteContentsRequest request = new DeleteContentsRequest(contentIds);

      when(userDetails.getId()).thenReturn(userId);

      // when
      ResponseEntity<Void> response = playlistController.deleteContents(playlistId, request, userDetails);

      // then
      assertEquals(204, response.getStatusCodeValue()); // NO_CONTENT
      assertNull(response.getBody());
      verify(playlistService).deleteContents(playlistId, contentIds, userId);
    }

    @Test
    @DisplayName("존재하지 않는 플레이리스트")
    void failsWhenPlaylistNotFound() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      List<UUID> contentIds = List.of(UUID.randomUUID());

      DeleteContentsRequest request = new DeleteContentsRequest(contentIds);

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new PlaylistNotFoundException())
          .when(playlistService).deleteContents(playlistId, contentIds, userId);

      // when & then
      assertThrows(PlaylistNotFoundException.class,
          () -> playlistController.deleteContents(playlistId, request, userDetails));
    }

    @Test
    @DisplayName("다른 사용자의 플레이리스트에서 컨텐츠 삭제 시도")
    void failsWhenAccessDenied() {
      // given
      UUID playlistId = UUID.randomUUID();
      UUID userId = UUID.randomUUID();
      List<UUID> contentIds = List.of(UUID.randomUUID());

      DeleteContentsRequest request = new DeleteContentsRequest(contentIds);

      when(userDetails.getId()).thenReturn(userId);
      doThrow(new AccessDeniedException("본인의 플레이리스트만 수정할 수 있습니다"))
          .when(playlistService).deleteContents(playlistId, contentIds, userId);

      // when & then
      assertThrows(AccessDeniedException.class,
          () -> playlistController.deleteContents(playlistId, request, userDetails));
    }
  }
}
