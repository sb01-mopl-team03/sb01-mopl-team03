package team03.mopl.domain.chat.handler;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

//테스트용 stomp 세션 handler
public class TestStompSessionHandler extends StompSessionHandlerAdapter {

  private final String sessionName;

  public TestStompSessionHandler() {
    this("default");
  }

  public TestStompSessionHandler(String sessionName) {
    this.sessionName = sessionName;
  }

  @Override
  public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
    System.out.println("[" + sessionName + "] Connected to WebSocket: " + session.getSessionId());
    System.out.println("[" + sessionName + "] Connected headers: " + connectedHeaders);
  }

  @Override
  public void handleException(StompSession session, StompCommand command,
      StompHeaders headers, byte[] payload, Throwable exception) {
    System.err.println("[" + sessionName + "] STOMP error: " + exception.getMessage());
    System.err.println("[" + sessionName + "] Command: " + command);
    System.err.println("[" + sessionName + "] Headers: " + headers);
    exception.printStackTrace();
  }

  @Override
  public void handleTransportError(StompSession session, Throwable exception) {
    System.err.println("[" + sessionName + "] Transport error: " + exception.getMessage());
    exception.printStackTrace();
  }


}
