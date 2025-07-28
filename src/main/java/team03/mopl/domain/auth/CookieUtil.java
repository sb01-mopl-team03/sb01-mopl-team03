package team03.mopl.domain.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;

public class CookieUtil {

  public static Cookie createResponseCookie(HttpServletResponse response,String refreshToken, long maxAgeInMs) {
    /*Cookie cookie = new Cookie("refresh", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (maxAgeInMs / 1000));
    return cookie;*/
    String cookieValue = ResponseCookie.from("refresh", refreshToken)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite("None")
        .maxAge(maxAgeInMs / 1000)
        .build()
        .toString();
    response.addHeader("Set-Cookie", cookieValue);
  }

  public static void deleteResponseCookie(Cookie cookie) {

  }
}
