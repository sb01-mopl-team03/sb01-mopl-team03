package team03.mopl.jwt;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

public class MemoryJwtBlacklist implements JwtBlacklist {

  private final Map<String ,Long> blacklist = new ConcurrentHashMap<>();

  @Override
  public void addBlacklist(String token, long expirationMs) {
    blacklist.put(token, expirationMs);
  }

  @Scheduled(fixedRate = 60 * 60 * 1000)
  public void cleanExpiredTokens() {
    long now = Instant.now().toEpochMilli();
    blacklist.entrySet().removeIf(entry ->  entry.getValue() < now);
  }

  @Override
  public boolean blacklisted(String token) {
    return blacklist.containsKey(token);
  }
}
