package team03.mopl.domain.auth;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import team03.mopl.common.config.SecurityConfig;
import team03.mopl.domain.oauth2.CustomOAuth2UserService;
import team03.mopl.domain.oauth2.OAuth2SuccessHandler;
import team03.mopl.jwt.CustomUserDetails;
import team03.mopl.jwt.CustomUserDetailsService;
import team03.mopl.jwt.JwtBlacklist;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.TokenPair;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthService authService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtProvider jwtProvider;

  @MockitoBean
  private JwtBlacklist jwtBlacklist;

  @MockitoBean
  private CustomUserDetailsService customUserDetailsService;

  @MockitoBean
  private CustomOAuth2UserService customOAuth2UserService;

  @MockitoBean
  private OAuth2SuccessHandler oAuth2SuccessHandler;

  private Cookie makeRefreshCookie(String value) {
    Cookie cookie = new Cookie("refresh", value);
    cookie.setPath("/");
    return cookie;
  }

  @Test
  void login() throws Exception {
    LoginRequest request = new LoginRequest("test@email.com", "password");
    LoginResult result = new LoginResult("access", "refresh", false);

    when(authService.login(request.email(), request.password())).thenReturn(result);

    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("access"))
        .andExpect(jsonPath("$.isTempPassword").value(false));
  }

  @Test
  @WithMockUser
  void logout() throws Exception {
    mockMvc.perform(post("/api/auth/logout")
            .cookie(makeRefreshCookie("refresh"))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string("LOGOUT"));

    verify(authService).invalidateSessionByRefreshToken("refresh", true);
  }

  @Test
  @WithMockUser
  void logoutFailWhenNoRefreshToken() throws Exception {
    mockMvc.perform(post("/api/auth/logout")
            .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("No refresh token"));
  }

  @Test
  @WithMockUser
  void refresh() throws Exception {
    TokenPair tokenPair = new TokenPair("new-access", "new-refresh");

    when(authService.refresh("refresh")).thenReturn(tokenPair);

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(makeRefreshCookie("refresh"))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string("new-access"));
  }

  @Test
  @WithMockUser
  void refreshFailNoToken() throws Exception {
    mockMvc.perform(post("/api/auth/refresh")
            .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("No refresh token found"));
  }

  @Test
  void refreshFailIllegalState() throws Exception {
    when(authService.refresh("expired-token")).thenThrow(new IllegalStateException());

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(makeRefreshCookie("expired-token"))
            .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("세션 없음 또는 만료됨"));
  }

  @Test
  @WithMockUser
  void reissueAccessToken() throws Exception {
    when(authService.reissueAccessToken("refresh"))
        .thenReturn(Optional.of("new-access-token"));

    mockMvc.perform(post("/api/auth/me")
            .cookie(makeRefreshCookie("refresh"))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string("new-access-token"));
  }

  @Test
  @WithMockUser
  void reissueAccessTokenFailNoToken() throws Exception {
    mockMvc.perform(post("/api/auth/me")
            .with(csrf()))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string("No refresh token"));
  }

  @Test
  @WithMockUser
  void resetPassword() throws Exception {
    TempPasswordRequest request = new TempPasswordRequest("user@email.com");

    mockMvc.perform(post("/api/auth/temp-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("임시 비밀번호 발급"));

    verify(authService).resetPassword("user@email.com");
  }

  @Test
  @WithMockUser
  void changePassword() throws Exception {
    UUID userId = UUID.randomUUID();
    ChangePasswordRequest request = new ChangePasswordRequest("newPw");

    CustomUserDetails principal = mock(CustomUserDetails.class);
    when(principal.getId()).thenReturn(userId);

    mockMvc.perform(post("/api/auth/change-password")
            .with(user(principal))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(content().string("비밀번호 변경완료"));

    verify(authService).changePassword(userId, "newPw");
  }
}