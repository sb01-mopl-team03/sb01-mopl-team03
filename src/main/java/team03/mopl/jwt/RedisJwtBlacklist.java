package team03.mopl.jwt;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisJwtBlacklist implements JwtBlacklist {

  private final RedisTemplate<String, String> redisTemplate;

  private static final String BLACKLIST_PREFIX = "blacklist:";

  @Override
  public void addBlacklist(String token, long accessTokenExpiration) {
    redisTemplate.opsForValue()
        .set(BLACKLIST_PREFIX + token, "logout", accessTokenExpiration, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean blacklisted(String token) {
    String key = "blacklist:" + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }
}
