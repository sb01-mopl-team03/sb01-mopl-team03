package team03.mopl.domain.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.UserResponse;

@WebMvcTest(controllers = AdminController.class)
class AdminControllerTest {

  @Autowired
  MockMvc mockMvc;

  @MockitoBean
  AdminService adminService;

  @Autowired
  ObjectMapper objectMapper;

  @Test
  @WithMockUser(roles = "ADMIN")
  void updateUserRole() throws Exception {
    UUID userId = UUID.randomUUID();
    RoleUpdateRequest request = new RoleUpdateRequest(userId, Role.ADMIN);
    UserResponse response = new UserResponse("admin@test.com", "테스터", "ADMIN", false, "image.png");

    when(adminService.changeRole(any(), any())).thenReturn(response);

    mockMvc.perform(put("/api/admin/role")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void lockUser() throws Exception {
    UUID userId = UUID.randomUUID();
    LockRequest request = new LockRequest(userId);
    UserResponse response = new UserResponse("admin@test.com", "테스터", "ADMIN", false, "image.png");

    when(adminService.lockUser(any())).thenReturn(response);

    mockMvc.perform(put("/api/admin/lock")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void unlockUser() throws Exception {
    UUID userId = UUID.randomUUID();
    LockRequest request = new LockRequest(userId);
    UserResponse response = new UserResponse("admin@test.com", "테스터", "ADMIN", false, "image.png");

    when(adminService.unlockUser(any())).thenReturn(response);

    mockMvc.perform(put("/api/admin/unlock")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }
}