package team03.mopl.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import team03.mopl.domain.user.User;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

  private final JwtBlacklist jwtBlacklist;
  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.access-token-expiration}")
  private long accessTokenExpiration;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExpiration;

  public String generateToken(User user) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + accessTokenExpiration);

    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("type","access")
        .claim("email",user.getEmail())
        .claim("role",user.getRole().name())
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(User user) {
    Date now = new Date();
    Date expiration = new Date(now.getTime() + refreshTokenExpiration);

    return Jwts.builder()
        .setSubject(user.getId().toString())
        .claim("type","refresh")
        .claim("email",user.getEmail())
        .claim("role",user.getRole().name())
        .setIssuedAt(now)
        .setExpiration(expiration)
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }


  public boolean validateToken(String token) {
    if (jwtBlacklist.blacklisted(token)){
      return false;
    }
    try{
      Jwts.parser()
          .verifyWith(getSigningKey())
          .build()
          .parseClaimsJws(token);
      return true;
    }catch (ExpiredJwtException e){
      log.warn("토큰이 만료되었습니다. : {}" , e.getMessage());
      return false;
    }catch (JwtException e){
      log.warn("유효하지 않는 토큰입니다. : {}" , e.getMessage());
      return false;
    }
  }

  public Claims getClaims(String token) {
    return Jwts.parser()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  public String extractEmail(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload()
        .get("email",String.class);
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }
}
