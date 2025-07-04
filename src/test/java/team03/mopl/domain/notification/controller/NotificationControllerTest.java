package team03.mopl.domain.notification.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import team03.mopl.domain.notification.dto.NotificationDto;
import team03.mopl.domain.notification.entity.NotificationType;
import team03.mopl.domain.notification.service.NotificationService;
import team03.mopl.domain.notification.service.SseEmitterManager;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  NotificationService notificationService;

  @MockitoBean
  SseEmitterManager sseEmitterManager;

  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testSubscribe() throws Exception {
    UUID userId = UUID.randomUUID();
    SseEmitter emitter = new SseEmitter();

    when(sseEmitterManager.subscribe(userId)).thenReturn(emitter);

    mockMvc.perform(get("/notifications/subscribe/{userId}", userId))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(username = "testuser", roles = "USER")
  void testGetNotifications() throws Exception {
    UUID userId = UUID.randomUUID();

    NotificationDto dto = new NotificationDto(
        UUID.randomUUID(),
        "새로운 알림입니다.",
        NotificationType.DM_RECEIVED,
        LocalDateTime.of(2025, 7, 1, 12, 0)
    );

    List<NotificationDto> notifications = List.of(dto);

    when(notificationService.getNotifications(userId)).thenReturn(notifications);

    mockMvc.perform(get("/notifications/{userId}", userId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].content").value("새로운 알림입니다."))
        .andExpect(jsonPath("$[0].notificationType").value("DM_RECEIVED"))
        .andExpect(jsonPath("$[0].createdAt").exists())
        .andExpect(jsonPath("$[0].id").value(dto.getId().toString()));

    verify(notificationService).markAllAsRead(userId);
  }
}
