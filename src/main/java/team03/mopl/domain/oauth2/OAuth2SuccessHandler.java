package team03.mopl.domain.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import team03.mopl.domain.auth.CookieUtil;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.CustomUserDetails;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

  private final JwtService jwtService;
  private final JwtProvider jwtProvider;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  @Value("${frontend.redirect-uri}")
  private String redirectUri;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    try
    {
      CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
      User user = userDetails.getUser();

      String accessToken = jwtProvider.generateToken(user);
      String refreshToken = jwtProvider.generateRefreshToken(user);
      jwtService.save(user, accessToken, refreshToken, refreshTokenExpiration);

      Cookie cookie = CookieUtil.createResponseCookie(refreshToken, refreshTokenExpiration);
      response.addCookie(cookie);

      String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
          .queryParam("access_token", accessToken)
          .build().toUriString();

      response.sendRedirect(redirectUrl);
    }catch (Exception e){
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED,"OAuth2 Success Handling failed");
    }

  }
}
