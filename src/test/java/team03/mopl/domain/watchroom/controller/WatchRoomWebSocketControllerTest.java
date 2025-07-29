package team03.mopl.domain.watchroom.controller;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import team03.mopl.common.exception.watchroom.UnsupportedVideoControlActionException;
import team03.mopl.common.exception.watchroom.VideoControlPermissionDeniedException;
import team03.mopl.domain.watchroom.dto.video.VideoControlRequest;
import team03.mopl.domain.watchroom.dto.video.VideoSyncDto;
import team03.mopl.domain.watchroom.dto.watchroommessage.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.watchroommessage.WatchRoomMessageDto;
import team03.mopl.domain.watchroom.entity.VideoControlAction;
import team03.mopl.domain.watchroom.service.WatchRoomMessageService;
import team03.mopl.domain.watchroom.service.WatchRoomService;

@ExtendWith(MockitoExtension.class)
class WatchRoomWebSocketControllerTest {

  @Mock
  private WatchRoomMessageService watchRoomMessageService;

  @Mock
  private WatchRoomService watchRoomService;

  @Mock
  private SimpMessagingTemplate messagingTemplate;

  @InjectMocks
  private WatchRoomWebSocketController watchRoomWebSocketController;

  @Nested
  @DisplayName("채팅방 메세지 전송 요청")
  class sendMessage {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      String username = "테스트유저이름";
      String content = "테스트 메세지입니다.";

      Principal principal = new Principal() {
        @Override
        public String getName() {
          return username;
        }
      };

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          mockRoomId,
          content,
          LocalDateTime.now()
      );
      //when & then
      WatchRoomMessageDto watchRoomMessageDto = watchRoomWebSocketController.sendMessage(mockRoomId,
          request, principal);

      verify(watchRoomMessageService).create(request, username);
    }

    @Test
    @DisplayName("요청 파라미터로 주어진 채팅방 id와 메세지 생성 요청 바디의 채팅방 Id가 일치하지 않음")
    void fail() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      String content = "테스트 메세지입니다.";

      Principal principal = mock(Principal.class);

      WatchRoomMessageCreateRequest request = new WatchRoomMessageCreateRequest(
          UUID.randomUUID(),
          content,
          LocalDateTime.now()
      );
      //when & then
      assertThrows(IllegalArgumentException.class,
          () -> watchRoomWebSocketController.sendMessage(mockRoomId, request, principal));

    }

  }

  @Nested
  @DisplayName("채팅방 입장 요청")
  class join {

    @Test
    @DisplayName("성공")
    void success() {
      //given
      UUID mockRoomId = UUID.randomUUID();
      String mockUserName = "test@test.com";

      Principal principal = new Principal() {
        @Override
        public String getName() {
          return mockUserName;
        }
      };

      //when
      watchRoomWebSocketController.joinRoom(mockRoomId, principal);

      //then
      verify(watchRoomService).joinWatchRoomAndGetInfo(mockRoomId, principal.getName());
      verify(watchRoomService).getParticipants(mockRoomId);

    }
  }

  @Nested
  @DisplayName("시청방 비디오 제어")
  class VideoControl {

    @Test
    @DisplayName("성공")
    void success() throws Exception {
      //given
      UUID roomId = UUID.randomUUID();

      Principal principal = mock(Principal.class);

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.PAUSE, 10.0);

      VideoSyncDto videoSyncDto = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.PAUSE)
          .currentTime(10.0)
          .isPlaying(false)
          .build();

      when(principal.getName()).thenReturn("test@test.com");
      when(watchRoomService.updateVideoStatus(roomId, request, principal.getName())).thenReturn(
          videoSyncDto);

      //when
      watchRoomWebSocketController.videoControl(roomId, request, principal);

      //then
      verify(watchRoomService).updateVideoStatus(roomId, request, principal.getName());
    }

    @Test
    @DisplayName("방장이 아닌 참여자는 제어할 수 없음")
    void failsWhenNotOwner() throws Exception {
      //given
      UUID roomId = UUID.randomUUID();

      Principal principal = mock(Principal.class);

      VideoControlRequest request = new VideoControlRequest(VideoControlAction.PAUSE, 10.0);

      VideoSyncDto videoSyncDto = VideoSyncDto.builder()
          .videoControlAction(VideoControlAction.PAUSE)
          .currentTime(10.0)
          .isPlaying(false)
          .build();

      when(principal.getName()).thenReturn("test@test.com");
      when(watchRoomService.updateVideoStatus(roomId, request, principal.getName()))
          .thenThrow(VideoControlPermissionDeniedException.class);

      //when & then
      assertThrows(VideoControlPermissionDeniedException.class,
          () -> watchRoomWebSocketController.videoControl(roomId, request, principal));
    }

    @Test
    @DisplayName("지원하지 않는 제어 액션")
    void failsWhenUnsupportedVideoControlAction() throws Exception {
      //given
      UUID roomId = UUID.randomUUID();
      Principal principal = mock(Principal.class);

      // Reflection으로 잘못된 enum 값 생성
      VideoControlAction invalidAction = mock(VideoControlAction.class);

      VideoControlRequest request = new VideoControlRequest(invalidAction, 10.0);

      when(principal.getName()).thenReturn("test@test.com");
      when(watchRoomService.updateVideoStatus(roomId, request, principal.getName()))
          .thenThrow(UnsupportedVideoControlActionException.class);

      //when & then
      assertThrows(UnsupportedVideoControlActionException.class,
          () -> watchRoomWebSocketController.videoControl(roomId, request, principal));
    }
  }
}