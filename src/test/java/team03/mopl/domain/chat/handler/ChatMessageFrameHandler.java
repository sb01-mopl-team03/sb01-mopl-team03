package team03.mopl.domain.chat.handler;

import java.util.concurrent.BlockingQueue;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import team03.mopl.domain.chat.dto.ChatMessageDto;
import java.lang.reflect.Type;

//채팅 메세지용 STOMP Frame Handler
public class ChatMessageFrameHandler implements StompFrameHandler {

  private final BlockingQueue<ChatMessageDto> messageQueue;

  public ChatMessageFrameHandler(BlockingQueue<ChatMessageDto> messageQueue) {
    this.messageQueue = messageQueue;
  }

  @Override
  public Type getPayloadType(StompHeaders headers) {
    return ChatMessageDto.class;
  }

  @Override
  public void handleFrame(StompHeaders headers, Object payload) {
    if (payload instanceof ChatMessageDto chatMessage) {
      messageQueue.offer(chatMessage);
      System.out.println("Received chat message: " + chatMessage.content());
    }
  }
}
