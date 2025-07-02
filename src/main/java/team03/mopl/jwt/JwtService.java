package team03.mopl.jwt;

import java.util.Optional;
import team03.mopl.domain.user.User;

public interface JwtService {

  void save(User user, String accessToken, String refreshToken, long refreshTokenExpirationMillis);

  void delete(User user);

  Optional<String> getAccessTokenByRefreshToken(String refreshToken);

  TokenPair reissueTokenPair(String refreshToken, long refreshTokenExpiration);

  void invalidateSessionByRefreshToken(String refreshToken ,boolean useBlacklist);
}
