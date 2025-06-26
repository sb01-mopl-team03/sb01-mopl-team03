package team03.mopl.jwt;

import team03.mopl.domain.user.User;

public interface JwtService {
  void save(User user,String accessToken,String refreshToken,long refreshTokenExpirationMillis);
  void delete(User user);
}
