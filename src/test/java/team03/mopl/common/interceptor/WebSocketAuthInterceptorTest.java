package team03.mopl.common.interceptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.jwt.CustomUserDetails;
import team03.mopl.jwt.CustomUserDetailsService;
import team03.mopl.jwt.JwtProvider;

public class WebSocketAuthInterceptorTest {

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private CustomUserDetailsService userDetailsService;

  @InjectMocks
  private WebSocketAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    interceptor = new WebSocketAuthInterceptor(jwtProvider, userDetailsService);
  }

  // STOMP 메시지를 생성하기 위한 메서드
  private Message<byte[]> createMessageWithHeader(StompCommand command, String token) {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
    accessor.setSessionId("test-session-id"); // 세션 ID 설정
    accessor.setLeaveMutable(true); // 헤더 변경 가능하도록 설정

    if (token != null) {
      accessor.addNativeHeader("Authorization", token);
    }
    // 헤더 정보를 그대로 유지한 채 Message 객체만 생성한다.
    return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
  }


  @Test
  @DisplayName("CONNECT 요청에서 유효한 토큰으로 인증 성공")
  void testConnectWithValidToken() {
    //given
    String token = "valid-token";
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .id(userId)
        .name("test")
        .email("test@test.com")
        .role(Role.USER)
        .build();

    CustomUserDetails userDetails = new CustomUserDetails(user);

    when(jwtProvider.validateToken(token)).thenReturn(true);
    when(jwtProvider.extractUserId(token)).thenReturn(userId);
    when(userDetailsService.loadUserById(userId)).thenReturn(userDetails);

    Message<byte[]> message = createMessageWithHeader(StompCommand.CONNECT, "Bearer " + token);

    //when
    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class)); //메세지 보냈을 때 인터셉터 실행

    //then - 인증 객체가 설정되어 있는지 확인
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
    Authentication authentication = (Authentication) accessor.getUser();

    assertNotNull(authentication);  //인증 정보 존재해야하고
    assertEquals("test@test.com", authentication.getName()); //사용자 이메일이랑 같아야함
  }

  @Test
  @DisplayName("CONNECT 요청에서 유효하지 않은 토큰인 경우 인증 실패 (null 반환 아님)")
  void testConnectWithInvalidToken() {
    // given
    String token = "invalid-token";

    when(jwtProvider.validateToken(token)).thenReturn(false);

    Message<byte[]> message = createMessageWithHeader(StompCommand.CONNECT, "Bearer " + token);

    //when
    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

    // then
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
    assertNull(accessor.getUser()); // 인증 정보가 없어야 한다. - 유효하지 않은 토큰이므로!
  }

  @Test
  @DisplayName("CONNECT 요청에서 Authorization 헤더 없는 경우")
  void testConnectWithoutAuthorizationHeader() {
    // given
    Message<byte[]> message = createMessageWithHeader(StompCommand.CONNECT, null);

    // when
    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

    //then
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
    assertNull(accessor.getUser());
  }

  @Test
  @DisplayName("SUBSCRIBE 요청 처리도 CONNECT와 동일하게 처리")
  void testSubscribeWithValidToken() {
    //SUBSCRIBE 요청은 특정 채널(DmRoom)에 해당 유저가 구독 가능한지 검증하기 위해서 유저 정보 필요하므로 인증 처리가 필요
    //given
    String token = "valid-token";
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .id(userId)
        .name("test")
        .email("test@test.com")
        .role(Role.USER)
        .build();

    CustomUserDetails userDetails = new CustomUserDetails(user);

    when(jwtProvider.validateToken(token)).thenReturn(true);
    when(jwtProvider.extractUserId(token)).thenReturn(userId);
    when(userDetailsService.loadUserById(userId)).thenReturn(userDetails);

    Message<byte[]> message = createMessageWithHeader(StompCommand.SUBSCRIBE, "Bearer " + token);

    // when
    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

    //then
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
    Authentication auth = (Authentication) accessor.getUser();
    assertNotNull(auth);
    assertEquals("test@test.com", auth.getName());
  }

  @Test
  @DisplayName("SEND 요청은 무시하고 인증하지 않음")
  void testSendCommandIgnored() {
    // 이미 CONNECT나 SUBSCRIBE에서 인증이 되었다면, 이후 메시지 전송 시 accessor.getUser()로 사용자 정보 확인 가능.
    // 따라서 굳이 매번 인증하지 않아도 됨

    //given
    Message<byte[]> message = createMessageWithHeader(StompCommand.SEND, "Bearer token");

    //when
    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

    //then
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(result);
    assertNull(accessor.getUser());
  }
}