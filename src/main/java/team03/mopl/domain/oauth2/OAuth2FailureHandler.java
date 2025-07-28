package team03.mopl.domain.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

  @Value("${frontend.redirect-uri}")
  private String redirectUri;

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception) throws IOException, ServletException {

    exception.printStackTrace();

    String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);

    String redirectUrl = UriComponentsBuilder.fromUriString(redirectUri)
        .queryParam("error", errorMessage).build().toUriString();

    response.sendRedirect(redirectUrl);
  }
}
