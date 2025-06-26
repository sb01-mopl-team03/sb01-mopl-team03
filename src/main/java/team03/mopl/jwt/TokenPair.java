package team03.mopl.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenPair {
  private final String accessToken;
  private final String refreshToken;
}
