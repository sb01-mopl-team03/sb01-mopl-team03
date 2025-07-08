package team03.mopl.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import team03.mopl.common.interceptor.WebSocketAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  private final WebSocketAuthInterceptor webSocketAuthInterceptor;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // WebSocket 연결용 엔드포인트
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    //모든 웹소켓 연결을 허용할 것인지도 확인해봐야할 듯?
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 클라이언트가 구독할 경로 (ex: /topic/chat.room.{roomId})
    // topic - 브로드캐스트용 / queue - 개인 전송용
    registry.enableSimpleBroker("/topic", "/queue");

    // 클라이언트가 메시지를 보낼 때 사용할 prefix
    registry.setApplicationDestinationPrefixes("/app");

    // 사용자별 메시지를 위한 prefix 설정
    // 스프링이 자동으로 변환해준다.
    //"/queue/sync" → "/user/admin@mopl.com/queue/sync"
    registry.setUserDestinationPrefix("/user");
  }

  // WebSocket 통신을 하려는 유저가 인증된 사용자인지 검증하기 위해 interceptor 구현
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(webSocketAuthInterceptor);
  }
}
