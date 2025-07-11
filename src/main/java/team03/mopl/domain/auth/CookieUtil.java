package team03.mopl.domain.auth;

import jakarta.servlet.http.Cookie;

public class CookieUtil {

  public static Cookie createResponseCookie(String refreshToken, long maxAgeInMs) {
    Cookie cookie = new Cookie("refresh", refreshToken);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (maxAgeInMs / 1000));
    return cookie;
  }

  public static void deleteResponseCookie(Cookie cookie) {

  }
}
