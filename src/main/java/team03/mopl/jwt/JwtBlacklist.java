package team03.mopl.jwt;

public interface JwtBlacklist {
  void addBlacklist(String token, long expirationMs);
  boolean blacklisted(String token);
}
