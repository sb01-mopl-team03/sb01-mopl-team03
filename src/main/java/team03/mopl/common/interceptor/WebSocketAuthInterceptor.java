package team03.mopl.common.interceptor;

import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.CustomUserDetails;
import team03.mopl.jwt.CustomUserDetailsService;
import team03.mopl.jwt.JwtProvider;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JwtProvider jwtProvider;
  private final CustomUserDetailsService customUserDetailsService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    log.info("=== WebSocket Message Debug ===");
    log.info("Message Type: {}", accessor.getMessageType());
    log.info("STOMP Command: {}", accessor.getCommand());
    log.info("Session ID: {}", accessor.getSessionId());

    // CONNECT 명령이 아니면 통과
    if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
      log.info("Non-CONNECT message, passing through");
      return message;
    }

    log.info("Processing CONNECT message");

    try {
      String authToken = accessor.getFirstNativeHeader("Authorization");
      log.info("Authorization header present: {}", authToken != null);

      if (authToken != null && authToken.startsWith("Bearer ")) {
        String token = authToken.substring(7);
        log.info("Token extracted, length: {}", token.length());

        if (jwtProvider.validateToken(token)) {
          log.info("JWT token validation successful");

          UUID userId = jwtProvider.extractUserId(token);
          log.info("Extracted user ID: {}", userId);

          CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);
          log.info("User details loaded: {}", userDetails.getUsername());

          Authentication auth = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          accessor.setUser(auth);

          log.info("WebSocket authentication successful for user: {}", userId);
        } else {
          log.warn("JWT token validation failed");
          // 토큰이 유효하지 않아도 일단 통과시켜보기
        }
      } else {
        log.warn("No valid Authorization header found");
        // Authorization 헤더가 없어도 일단 통과시켜보기
      }
    } catch (Exception e) {
      log.error("Error during WebSocket authentication: {}", e.getMessage(), e);
      // 예외가 발생해도 메시지를 반환 (연결 차단 방지)
    }

    return message;
  }
}