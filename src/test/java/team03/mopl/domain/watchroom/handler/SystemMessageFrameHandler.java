package team03.mopl.domain.watchroom.handler;


import java.util.concurrent.BlockingQueue;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import team03.mopl.domain.watchroom.dto.SystemMessageDto;
import java.lang.reflect.Type;

// 시스템 메세지용 STOMP Message Handler
public class SystemMessageFrameHandler implements StompFrameHandler {

  private final BlockingQueue<SystemMessageDto> systemQueue;

  public SystemMessageFrameHandler(BlockingQueue<SystemMessageDto> systemQueue) {
    this.systemQueue = systemQueue;
  }

  @Override
  public Type getPayloadType(StompHeaders headers) {
    return SystemMessageDto.class;
  }

  @Override
  public void handleFrame(StompHeaders headers, Object payload) {
    if (payload instanceof SystemMessageDto systemMessage) {
      systemQueue.offer(systemMessage);
      System.out.println("Received system message: " + systemMessage.message());
    }
  }
}
