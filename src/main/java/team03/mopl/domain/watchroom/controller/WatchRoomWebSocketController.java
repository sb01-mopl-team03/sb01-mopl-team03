package team03.mopl.domain.watchroom.controller;

import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageCreateRequest;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageDto;
import team03.mopl.domain.watchroom.dto.ParticipantsInfoDto;
import team03.mopl.domain.watchroom.dto.VideoControlRequest;
import team03.mopl.domain.watchroom.dto.VideoSyncDto;
import team03.mopl.domain.watchroom.dto.WatchRoomInfoDto;
import team03.mopl.domain.watchroom.service.WatchRoomMessageService;
import team03.mopl.domain.watchroom.service.WatchRoomService;

@Controller
@RequiredArgsConstructor
public class WatchRoomWebSocketController {

  private final WatchRoomService watchRoomService;
  private final WatchRoomMessageService watchRoomMessageService;
  private final SimpMessagingTemplate messageTemplate;

  //채팅 메세지 보내기
  @MessageMapping("/rooms/{roomId}/send")
  @SendTo("/topic/rooms/{roomId}/chat")
  public WatchRoomMessageDto sendMessage(@DestinationVariable UUID roomId,
      WatchRoomMessageCreateRequest request, Principal principal) {
    if (!request.chatRoomId().equals(roomId)) {
      //본문의 WatchRoom id와 url의 id가 같은지 한번 더 검증함
      throw new IllegalArgumentException("Room ID가 일치하지 않습니다.");
    }
    return watchRoomMessageService.create(request, principal.getName());
  }

  // 새 유저 참가 요청
  @MessageMapping("/rooms/{roomId}/join")
  public void joinRoom(@DestinationVariable UUID roomId, Principal principal) {

    //신규 유저에게는 채팅방 정보 전체 전송
    WatchRoomInfoDto watchRoomInfoDto = watchRoomService.joinWatchRoomAndGetInfo(roomId,
        principal.getName());
    messageTemplate.convertAndSendToUser(principal.getName(), "/queue/sync", watchRoomInfoDto);

    //기존 유저에게는 참여자 목록만 브로드캐스트
    ParticipantsInfoDto participantsInfoDto = watchRoomService.getParticipants(roomId);
    messageTemplate.convertAndSend("/topic/rooms/" + roomId + "/participants", participantsInfoDto);

  }

  //방장 비디오 제어
  @MessageMapping("/rooms/{roomId}/video-control")
  @SendTo("/topic/rooms/{roomId}/video")
  public VideoSyncDto videoControl(@DestinationVariable UUID roomId, VideoControlRequest request,
      Principal principal) {

    VideoSyncDto videoSyncDto = watchRoomService.updateVideoStatus(roomId, request,
        principal.getName());

    return videoSyncDto;
  }

  //나가기 요청
  @MessageMapping("/rooms/{roomId}/leave")
  @SendTo("/topic/rooms/{roomId}/participants")
  public ParticipantsInfoDto leaveWatchRoom(@DestinationVariable UUID roomId,
      Principal principal) {

    watchRoomService.leave(roomId, principal.getName());

    return watchRoomService.getParticipants(roomId);

  }

}
