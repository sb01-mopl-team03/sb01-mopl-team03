package team03.mopl.domain.notification.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.dto.NotificationDto;
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

  @Autowired
  NotificationService notificationService;


  @TestConfiguration
  public static class configuration {

    @Bean
    public NotificationService notificationService() {
      return mock(NotificationService.class);
    }

    @Bean
    public UserService userService() { return mock(UserService.class); }

    @Bean
    public EmitterService emitterService() { return mock(EmitterService.class); }
  }


  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testSubscribe() throws Exception {
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("testuser@example.com")
        .password("password")
        .role(Role.USER)
        .build();
    CustomUserDetails customUserDetails = new CustomUserDetails(user);

    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            customUserDetails,
            customUserDetails.getPassword(),
            customUserDetails.getAuthorities()
        );

    UUID userId = UUID.randomUUID();
    String lastNotificationId = String.valueOf(UUID.randomUUID());
    SseEmitter emitter = new SseEmitter();

    when(emitterService.subscribe(userId, lastNotificationId)).thenReturn(emitter);

    mockMvc.perform(get("/notifications/subscribe")
            .with(authentication(authToken)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser
  void testGetNotifications() throws Exception {
    User user = User.builder()
        .id(UUID.randomUUID())
        .email("testuser@example.com")
        .password("password")
        .role(Role.USER)
        .build();
    CustomUserDetails customUserDetails = new CustomUserDetails(user);

    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(
            customUserDetails,
            customUserDetails.getPassword(),
            customUserDetails.getAuthorities()
        );

    NotificationDto dto = new NotificationDto(
        user.getId(),
        "새로운 알림입니다.",
        NotificationType.DM_RECEIVED,
        LocalDateTime.of(2025, 7, 1, 12, 0)
    );

    List<NotificationDto> notifications = List.of(dto);

    when(notificationService.getNotifications(user.getId())).thenReturn(notifications);

    mockMvc.perform(get("/notifications")
            .with(authentication(authToken))
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].content").value("새로운 알림입니다."))
        .andExpect(jsonPath("$[0].notificationType").value("DM_RECEIVED"))
        .andExpect(jsonPath("$[0].createdAt").exists())
        .andExpect(jsonPath("$[0].receiverId").value(dto.getReceiverId().toString()));


    verify(notificationService).markAllAsRead(user.getId());
  }
}
