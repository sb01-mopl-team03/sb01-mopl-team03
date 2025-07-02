package team03.mopl.domain.dm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket 연결용 엔드포인트
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    //모든 웹소켓 연결을 허용할 것인지도 확인해봐야할 듯?
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 클라이언트가 구독할 경로 (ex: /topic/chat.room.{roomId})
    registry.enableSimpleBroker("/topic");

    // 클라이언트가 메시지를 보낼 때 사용할 prefix
    registry.setApplicationDestinationPrefixes("/app");
  }
}
