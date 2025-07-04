package team03.mopl.domain.chat.controller;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import team03.mopl.common.config.TestSecurityConfig;
import team03.mopl.domain.chat.dto.ChatMessageDto;
import team03.mopl.domain.chat.handler.ChatMessageFrameHandler;
import team03.mopl.domain.chat.handler.TestStompSessionHandler;
import team03.mopl.domain.chat.service.ChatMessageService;
import team03.mopl.domain.chat.service.ChatRoomService;
import team03.mopl.domain.curation.service.CurationService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ChatMessageControllerWebSocketTest {

  @LocalServerPort
  private int port;

  @MockitoBean
  private ChatRoomService chatRoomService;

  @MockitoBean
  private ChatMessageService chatMessageService;

  //StanfordCoreNLP 엔진을 초기화할 때 대규모 리소스를 적재함 -> OOM 발생
  //curationService Mock 처리하여 init실행하지 않도록 설정
  @MockitoBean
  private CurationService curationService;


  private StompSession stompSession;
  private WebSocketStompClient stompClient;
  private final BlockingQueue<ChatMessageDto> messageQueue = new ArrayBlockingQueue<>(10);

  @BeforeEach
  void setup() throws Exception {
    // WebSocket 클라이언트 설정만
    List<Transport> transports = List.of(
        new WebSocketTransport(new StandardWebSocketClient())
    );
    SockJsClient sockJsClient = new SockJsClient(transports);

    stompClient = new WebSocketStompClient(sockJsClient);
    stompClient.setMessageConverter(new MappingJackson2MessageConverter());

    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.afterPropertiesSet();
    stompClient.setTaskScheduler(taskScheduler);

    // 연결
    CompletableFuture<StompSession> future = stompClient.connectAsync(
        URI.create("ws://localhost:" + port + "/ws"),
        null,
        new StompHeaders(),
        new TestStompSessionHandler()
    );

    stompSession = future.get(5, TimeUnit.SECONDS);
  }

  @Test
  void testWebSocketConnection() {
    assertThat(stompSession.isConnected()).isTrue();
  }

  @Test
  void testWebSocketEndpointExists() throws Exception {
    // 엔드포인트 존재 확인
    StompSession.Subscription subscription = stompSession.subscribe(
        "/topic/rooms/" + UUID.randomUUID(),
        new ChatMessageFrameHandler(messageQueue)
    );

    assertThat(subscription).isNotNull();
    subscription.unsubscribe();
  }

  @AfterEach
  void cleanup() {
    if (stompSession != null && stompSession.isConnected()) {
      stompSession.disconnect();
    }
  }
}
