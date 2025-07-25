package team03.mopl.domain.dm.controller;

import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmRequest;
import team03.mopl.domain.dm.dto.SendDmDto;
import team03.mopl.domain.dm.service.DmService;
import team03.mopl.domain.dm.service.PresenceTracker;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DmWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final DmService dmService;
  private final PresenceTracker presenceTracker;

  @MessageMapping("/dm/{roomId}")
  public void sendMessage(@DestinationVariable String roomId, DmRequest dmRequest) {
    log.debug("sendMessage - WebSocket DM 전송 요청: senderId={}, roomId={}, content={}",
        dmRequest.getSenderId(), dmRequest.getRoomId(), dmRequest.getContent());

    DmDto saved = dmService.sendDm(
        new SendDmDto(dmRequest.getSenderId(), UUID.fromString(roomId), dmRequest.getContent())
    );
    log.debug("sendMessage - WebSocket DM 저장 완료 및 브로드캐스트: dmId={}, roomId={}",
        saved.getId(), saved.getRoomId());
    messagingTemplate.convertAndSend("/topic/dm/" + roomId, saved);
  }
  @MessageMapping("/dmRooms/{roomId}/enter}")
  public void enterRoom(@DestinationVariable String roomId, Principal principal) {
    presenceTracker.enterRoom(principal.getName(),UUID.fromString(roomId));
    log.info("enterRoom - 사용자 채팅방 접속: userName={}, roomId={}", principal.getName(), roomId);
  }
  @MessageMapping("/dmRooms/{roomId}/exit")
  public void exitRoom(@DestinationVariable String roomId, Principal principal) {
    presenceTracker.exitRoom(principal.getName(), UUID.fromString(roomId));
    log.info("exitRoom - 사용자 채팅방 퇴장: userName={}, roomId={}", principal.getName(), roomId);
  }
}
