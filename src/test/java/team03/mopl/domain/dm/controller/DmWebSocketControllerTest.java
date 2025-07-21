package team03.mopl.domain.dm.controller;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import team03.mopl.domain.dm.dto.DmDto;
import team03.mopl.domain.dm.dto.DmRequest;
import team03.mopl.domain.dm.entity.Dm;
import team03.mopl.domain.dm.entity.DmRoom;
import team03.mopl.domain.dm.service.DmService;

class DmWebSocketControllerTest {

  private DmService dmService;
  private SimpMessagingTemplate simpMessagingTemplate;
  private DmWebSocketController controller;

  @BeforeEach
  void setUp() {
    dmService = mock(DmService.class);
    simpMessagingTemplate = mock(SimpMessagingTemplate.class);
    controller = new DmWebSocketController(simpMessagingTemplate, dmService);
  }

  @Test
  void sendMessageTest() {
    UUID roomId = UUID.randomUUID();
    UUID senderId = UUID.randomUUID();
    UUID receiverId = UUID.randomUUID();
    String content = "hello";
    DmRequest dmRequest = new DmRequest(senderId, roomId, content, false, LocalDateTime.now());
    var dmRoom = new DmRoom(senderId, receiverId);
    ReflectionTestUtils.setField(dmRoom, "id", roomId);

    // Dm에 연관관계 주입
    Dm dm = new Dm(senderId, content);
    dm.setDmRoom(dmRoom);
    DmDto dmDto = DmDto.from(dm);
    ReflectionTestUtils.setField(dmDto, "id", UUID.randomUUID());
    when(dmService.sendDm(argThat(dto ->
        dto.getSenderId().equals(senderId) &&
            dto.getRoomId().equals(roomId) &&
            dto.getContent().equals(content)
    ))).thenReturn(dmDto);

    controller.sendMessage(roomId.toString(), dmRequest);

    verify(dmService).sendDm(argThat(dto ->
        dto.getSenderId().equals(senderId) &&
            dto.getRoomId().equals(roomId) &&
            dto.getContent().equals(content)
    ));
    verify(simpMessagingTemplate).convertAndSend(eq("/topic/dm/" + roomId), eq(dmDto));

  }
}
