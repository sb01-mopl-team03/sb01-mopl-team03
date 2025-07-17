package team03.mopl.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import team03.mopl.domain.oauth2.CustomOAuth2UserService;
import team03.mopl.domain.oauth2.OAuth2SuccessHandler;
import team03.mopl.jwt.CustomUserDetailsService;
import team03.mopl.jwt.JwtAuthenticationFilter;
import team03.mopl.jwt.JwtBlacklist;
import team03.mopl.jwt.JwtProvider;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomUserDetailsService customUserDetailsService;
  private final JwtProvider jwtProvider;
  private final JwtBlacklist jwtBlacklist;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final CorsConfigurationSource corsConfigurationSource;
  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/oauth2/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/change-password").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/temp-password").permitAll()
            .requestMatchers("/profile/**").permitAll()
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers("/swagger-ui*/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/error").permitAll()
            // SSE 엔드포인트를 permitAll로 설정
            .requestMatchers("/api/notifications/subscribe").permitAll()
                .anyRequest().permitAll()
//            .anyRequest().hasRole("USER")
        )
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(((request, response, authException) -> {
              if (response.isCommitted()) return;
              response.setStatus(401);
              response.setContentType("application/json;charset=utf-8");
              try {
                response.getWriter().write("{\"error\":\"Authentication required\"}");
              } catch (Exception e) {
                System.err.println("Auth error write failed: " + e.getMessage());
              }
            }))
            .accessDeniedHandler(((request, response, accessDeniedException) -> {
              if (response.isCommitted()) return;
              response.setStatus(403);
              response.setContentType("application/json;charset=utf-8");
              try {
                response.getWriter().write("{\"error\":\"Access denied\"}");
              } catch (Exception e) {
                System.err.println("Access denied error write failed: " + e.getMessage());
              }
            }))
        )
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService))
            .successHandler(oAuth2SuccessHandler))
        .addFilterBefore(
            new JwtAuthenticationFilter(jwtProvider, customUserDetailsService, jwtBlacklist),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return web -> web.ignoring()
        .requestMatchers("/profile/**")
        .requestMatchers("/ws/**");
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy("""
        ROLE_ADMIN > ROLE_USER
        """);
  }
}
