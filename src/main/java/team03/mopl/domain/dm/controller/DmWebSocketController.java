package team03.mopl.domain.dm.controller;

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

@Controller
@RequiredArgsConstructor
@Slf4j
public class DmWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final DmService dmService;

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
  /*@MessageMapping("/dm.send") // /app/dm.send
  public void sendMessage(DmRequest dmRequest) {
    log.debug("sendMessage - WebSocket DM 전송 요청: senderId={}, roomId={}, content={}",
        dmRequest.getSenderId(), dmRequest.getRoomId(), dmRequest.getContent());
    // 메시지를 DB에 저장
    DmDto saved = dmService.sendDm(new SendDmDto(dmRequest.getSenderId(), dmRequest.getRoomId(), dmRequest.getContent()));

    log.debug("sendMessage - WebSocket DM 저장 완료 및 브로드캐스트: dmId={}, roomId={}",
        saved.getId(), saved.getRoomId());

    // 저장된 값을 그대로 broadcast
    // 해당 방을 구독하고 있던 유저 모두에게 동기화
    messagingTemplate.convertAndSend("/topic/dm.room." + dmRequest.getRoomId(), saved);
  }*/
}
