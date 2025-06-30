package team03.mopl.domain.dm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmMessage;
import team03.mopl.domain.dm.service.DmService;

@Controller
@RequiredArgsConstructor
public class DmWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final DmService dmService;

  @MessageMapping("/dm.send") // /app/dm.send
  public void sendMessage(DmMessage dmMessage) {
    // 메시지를 DB에 저장
    DmDto saved = dmService.sendDm(
        dmMessage.getSenderId(),
        dmMessage.getRoomId(),
        dmMessage.getContent()
    );

    // 저장된 값을 그대로 broadcast
    messagingTemplate.convertAndSend(
        "/topic/dm.room." + dmMessage.getRoomId(),
        saved
    );
  }
}
