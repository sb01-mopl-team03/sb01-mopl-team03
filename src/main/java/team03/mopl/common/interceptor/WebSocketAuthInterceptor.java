package team03.mopl.common.interceptor;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
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

    log.debug("=== WebSocket Message Debug ===");
    log.debug("Message Type: {}", accessor.getMessageType());
    log.debug("STOMP Command: {}", accessor.getCommand());
    log.debug("Session ID: {}", accessor.getSessionId());

    // CONNECT 명령이 아니면 통과
    if (!StompCommand.CONNECT.equals(accessor.getCommand())) {
      log.info("Non-CONNECT message 처리 건너뜀");
      return message;
    }

    log.info("CONNECT message 처리 시작");

    try {
      String authToken = accessor.getFirstNativeHeader("Authorization");
      log.debug("Authorization header 존재 여부: authToken 존재 = {}", authToken != null);

      if (authToken != null && authToken.startsWith("Bearer ")) {
        String token = authToken.substring(7);
        log.debug("Token 추출: length = {}", token.length());

        if (jwtProvider.validateToken(token)) {
          log.debug("JWT token 검증 성공");

          UUID userId = jwtProvider.extractUserId(token);
          log.debug("사용자 ID 추출 : userId={}", userId);

          CustomUserDetails userDetails = customUserDetailsService.loadUserById(userId);
          log.debug("UserDetail 불러오기: username = {}", userDetails.getUsername());

          Authentication auth = new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities());
          accessor.setUser(auth);

          log.info("WebSocket authentication 성공 : userId = {}", userId);
        } else {
          log.warn("JWT token 유효성 검증 실패");
          // 토큰이 유효하지 않아도 일단 통과시켜보기
        }
      } else {
        log.warn("유효한 Authorization header를 찾을 수 없음");
        // Authorization 헤더가 없어도 일단 통과시켜보기
      }
    } catch (Exception e) {
      log.error("WebSocket 인증/인가 중 오류 발생: 오류 메세지 = {}", e.getMessage(), e);
      // 예외가 발생해도 메시지를 반환 (연결 차단 방지)
    }

    return message;
  }
}