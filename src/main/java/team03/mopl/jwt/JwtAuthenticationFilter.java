package team03.mopl.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final CustomUserDetailsService userDetailsService;
  private final JwtBlacklist jwtBlacklist;

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token=extractToken(request);

    if (token != null && jwtProvider.validateToken(token)) {
      if(jwtBlacklist.blacklisted(token)) {
        filterChain.doFilter(request, response);
        return;
      }


      UUID userId = jwtProvider.extractUserId(token);
      UserDetails userDetails = userDetailsService.loadUserById(userId);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userDetails, null, userDetails.getAuthorities()
          );

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    String uri = request.getRequestURI();
    return uri.startsWith("/profile/");
  }
}
