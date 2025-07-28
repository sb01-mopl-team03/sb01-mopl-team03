package team03.mopl.domain.notification.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.common.dto.Cursor;
import team03.mopl.common.dto.CursorPageResponseDto;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.dto.NotificationPagingDto;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.EmitterService;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserService;
import team03.mopl.jwt.CustomUserDetails;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

  @Autowired
  MockMvc mockMvc;

  @Mock
  private UserService userService;

  @Mock
  private EmitterService emitterService;

  @InjectMocks
  private NotificationController notificationController;

  @Autowired
  NotificationService notificationService;
  private UUID receiverId;

  @BeforeEach
  void setUp() {
    receiverId = UUID.randomUUID();
  }

  private CustomUserDetails principal(UUID userId) {
    User user = User.builder()
        .id(userId)
        .email("test@test.com")
        .name("tester")
        .password("pw")
        .role(Role.USER)
        .build();
    return new CustomUserDetails(user);
  }

  @TestConfiguration
  public static class configuration {

    @Bean
    public NotificationService notificationService() {
      return mock(NotificationService.class);
    }

    @Bean
    public UserService userService() {
      return mock(UserService.class);
    }

    @Bean
    public EmitterService emitterService() {
      return mock(EmitterService.class);
    }
  }


  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testSubscribe() throws Exception {
    User user = User.builder().id(UUID.randomUUID()).email("testuser@example.com").password("password").role(Role.USER).build();
    CustomUserDetails customUserDetails = new CustomUserDetails(user);

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, customUserDetails.getPassword(),
        customUserDetails.getAuthorities());

    UUID userId = UUID.randomUUID();
    String lastNotificationId = String.valueOf(UUID.randomUUID());
    SseEmitter emitter = new SseEmitter();

    when(emitterService.subscribe(userId, lastNotificationId)).thenReturn(emitter);

    mockMvc.perform(get("/api/notifications/subscribe").with(authentication(authToken))).andExpect(status().isOk());
  }

  @Test
  @DisplayName("subscribe – 인증 정보 없을 때, 즉시 에러 상태의 SseEmitter 반환")
  void subscribe_whenUserIsNull_shouldReturnErrorEmitter() throws Exception {
    // given: 인증 정보(null), Last-Event-ID 도 null
    HttpServletResponse response = mock(HttpServletResponse.class);

    // when
    SseEmitter emitter = notificationController.subscribe(null, null, response);

    // then: completeWithError(new RuntimeException("Unauthorized: 인증이 필요합니다")) 경로로 빠졌으므로,
    // 실패한 emitter는 테스트에서 인위적으로 send() 호출 시 이미 완료된 상태라 IllegalStateException을 던진다.
    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> emitter.send(SseEmitter.event().name("test")),
        "인증 실패 시 send()는 IllegalStateException을 발생시켜야 합니다");

    // 에러 메시지에 “Unauthorized” 포함 여부도 확인
    assertTrue(ex.getMessage().toLowerCase().contains("unauthorized"),
        "에러 메시지에 'Unauthorized'가 포함되어야 합니다");
  }


  @Test
  @DisplayName("case1 : emitter != null 인 상태에서 예외")
  void subscribe_whenErrorAfterEmitterCreated_callsCompleteWithError_andReturnsSameEmitter() {
    // given
    String lastEventId = "abc";
    SseEmitter spyEmitter = Mockito.spy(new SseEmitter());
    when(emitterService.subscribe(eq(receiverId), eq(lastEventId))).thenReturn(spyEmitter);

    // sendInitNotification에서 예외 유발 → catch 진입
    doThrow(new RuntimeException("SSE 구독 중 예외 발생")).when(emitterService).sendInitNotification(spyEmitter);

    // when
    SseEmitter returned = notificationController.subscribe(principal(receiverId), lastEventId, new MockHttpServletResponse());

    // then
    assertSame(spyEmitter, returned);
    verify(spyEmitter).completeWithError(any(Throwable.class));
  }

  @Test
  @DisplayName("subscribe - subscribe 중 예외로 emitter가 null인 경우: 새 emitter 생성 후 completeWithError")
  void subscribe_errorBeforeEmitterCreated_returnsNewCompletedEmitter() {
    // given
    UUID userId = UUID.randomUUID();
    User user = User.builder()
        .id(userId)
        .email("u@test.com").name("u").password("pw").role(Role.USER)
        .build();
    CustomUserDetails principal = new CustomUserDetails(user);
    String lastEventId = "abc";

    // subscribe 호출 자체에서 예외 발생 -> emitter는 null로 남음
    when(emitterService.subscribe(eq(userId), eq(lastEventId))).thenThrow(new RuntimeException("subscribe fail"));

    // when
    SseEmitter returned = notificationController.subscribe(principal, lastEventId, new MockHttpServletResponse());

    // then
    Assertions.assertNotNull(returned);
    System.out.println("returned = " + returned);

    // 이미 completeWithError 된 상태이므로 send하면 예외가 난다
    assertThrows(IllegalStateException.class,
        () -> returned.send(SseEmitter.event().comment("won't send")));
  }


  @Test
  @WithMockUser
  void testGetNotifications() throws Exception {
    User user = User.builder().id(UUID.randomUUID()).email("testuser@example.com").password("password").role(Role.USER).build();
    CustomUserDetails customUserDetails = new CustomUserDetails(user);
    UUID notificationId = UUID.randomUUID();
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(customUserDetails, customUserDetails.getPassword(),
        customUserDetails.getAuthorities());

    NotificationDto dto = new NotificationDto(notificationId, user.getId(), "새로운 알림입니다.", NotificationType.DM_RECEIVED,
        LocalDateTime.of(2025, 7, 1, 12, 0));

    List<NotificationDto> notifications = List.of(dto);

    var response = CursorPageResponseDto.<NotificationDto>builder().data(notifications).nextCursor(null).size(1).totalElements(1L).hasNext(false)
        .build();

    when(notificationService.getNotifications(any(NotificationPagingDto.class), eq(user.getId()))).thenReturn(response);

    mockMvc.perform(get("/api/notifications").with(authentication(authToken)).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].content").value("새로운 알림입니다."))
        .andExpect(jsonPath("$.data[0].notificationType").value("DM_RECEIVED"))
        .andExpect(jsonPath("$.data[0].createdAt").exists()).andExpect(jsonPath("$.data[0].receiverId").value(dto.getReceiverId().toString()));

    verify(notificationService).markAllAsRead(user.getId());
  }

  @Test
  @DisplayName("알림 목록 페이징 테스트 - 1페이지 20개, 2페이지 10개")
  void getNotificationPagingTest() throws Exception {
    // given
    List<NotificationDto> firstPageData = new ArrayList<>();
    List<NotificationDto> secondPageData = new ArrayList<>();
    LocalDateTime now = LocalDateTime.now();
    UUID notificationId = UUID.randomUUID();
    for (int i = 0; i < 30; i++) {
      NotificationDto notificationDto = new NotificationDto(notificationId, receiverId, "콘텐츠 " + i, NotificationType.DM_RECEIVED,
          now.minusSeconds(30 - i));
      if (i < 20) {
        firstPageData.add(notificationDto);
      } else {
        secondPageData.add(notificationDto);
      }
    }

    String encodedCursor = encodeCursor(
        new Cursor(secondPageData.get(0).getCreatedAt().toString(), secondPageData.get(0).getReceiverId().toString()));

    // 1페이지 mock 응답
    given(notificationService.getNotifications(argThat(dto -> dto != null && dto.getCursor() == null), eq(receiverId))).willReturn(
        CursorPageResponseDto.<NotificationDto>builder().data(firstPageData).nextCursor(encodedCursor).size(20).totalElements(30L).hasNext(true)
            .build());

    // 2페이지 mock 응답
    given(notificationService.getNotifications(argThat(dto -> dto != null && encodedCursor.equals(dto.getCursor())), eq(receiverId))).willReturn(
        CursorPageResponseDto.<NotificationDto>builder().data(secondPageData).nextCursor(null).size(10).totalElements(30L).hasNext(false).build());

    // SecurityContext 설정
    User user = User.builder().id(receiverId).email("user@test.com").name("user").password("pw").role(Role.USER).build();
    var customUserDetails = new CustomUserDetails(user);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities()));

    // when & then (1st page)
    mockMvc.perform(get("/api/notifications")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(20))
        .andExpect(jsonPath("$.hasNext").value(true))
        .andExpect(jsonPath("$.size").value(20));

    // when & then (2nd page)
    mockMvc.perform(get("/api/notifications?cursor={cursor}", encodedCursor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(10))
        .andExpect(jsonPath("$.hasNext").value(false))
        .andExpect(jsonPath("$.size").value(10));
  }

  private String encodeCursor(Cursor cursor) throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();
    String json = objectMapper.writeValueAsString(cursor);
    return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
  }

  @Test
  @DisplayName("DELETE /api/notifications/{id} : 특정 알림 삭제 -> 204, 서비스 호출 확인")
  void deleteNotification_success() throws Exception {
    // given
    UUID notifId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    var auth = new UsernamePasswordAuthenticationToken(principal(userId), null,
        principal(userId).getAuthorities());

    // when & then
    mockMvc.perform(delete("/api/notifications/{notificationId}", notifId)
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
        .andExpect(status().isNoContent());

    then(notificationService).should().deleteNotification(eq(notifId));
  }

  @Test
  @DisplayName("DELETE /api/notifications : 본인 알림 전체 삭제 -> 204, 서비스 호출 확인")
  void deleteNotificationByUser_success() throws Exception {
    // given
    UUID userId = UUID.randomUUID();
    var details = principal(userId);
    var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());

    // when & then
    mockMvc.perform(delete("/api/notifications")
            .with(csrf())
            .with(SecurityMockMvcRequestPostProcessors.authentication(auth)))
        .andExpect(status().isNoContent());

    then(notificationService).should().deleteNotificationByUserId(eq(userId));
  }


}
