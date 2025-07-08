package team03.mopl.domain.watchroom.handler;

import java.util.concurrent.BlockingQueue;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import team03.mopl.domain.watchroom.dto.WatchRoomMessageDto;
import java.lang.reflect.Type;

//채팅 메세지용 STOMP Frame Handler
public class ChatMessageFrameHandler implements StompFrameHandler {

  private final BlockingQueue<WatchRoomMessageDto> messageQueue;

  public ChatMessageFrameHandler(BlockingQueue<WatchRoomMessageDto> messageQueue) {
    this.messageQueue = messageQueue;
  }

  @Override
  public Type getPayloadType(StompHeaders headers) {
    return WatchRoomMessageDto.class;
  }

  @Override
  public void handleFrame(StompHeaders headers, Object payload) {
    if (payload instanceof WatchRoomMessageDto chatMessage) {
      messageQueue.offer(chatMessage);
      System.out.println("Received chat message: " + chatMessage.content());
    }
  }
}
