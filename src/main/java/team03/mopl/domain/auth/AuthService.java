package team03.mopl.domain.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.auth.InvalidPasswordException;
import team03.mopl.common.exception.auth.LockedUserException;
import team03.mopl.common.exception.auth.TempPasswordExpiredException;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.JwtSessionRepository;
import team03.mopl.jwt.TokenPair;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final JwtProvider jwtProvider;
  private final PasswordEncoder passwordEncoder;
  private final EmailService emailService;
  private final JwtSessionRepository jwtSessionRepository;

  @Value("${auth.temp-password-expiration}")
  private long tempPasswordExpirationMinutes;

  @Value("${jwt.refresh-token-expiration}")
  private long refreshTokenExp;

  public LoginResult login(String email, String password) {
    log.info("login - 로그인 시도: email={}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> {
          log.warn("존재하지 않는 사용자: email={}", email);
          return new UserNotFoundException();
        });

    if (user.isLocked()) {
      log.warn("잠긴 계정 로그인 시도: userId={}", user.getId());
      jwtSessionRepository.deleteByUser(user);
      throw new LockedUserException();
    }

    if (!passwordEncoder.matches(password, user.getPassword())) {
      log.warn("비밀번호 불일치: email={}", email);
      throw new InvalidPasswordException();
    }

    if (user.isTempPassword() &&
        user.getTempPasswordExpiredAt().isBefore(LocalDateTime.now())) {
      log.warn("임시 비밀번호 만료: userId={}", user.getId());
      throw new TempPasswordExpiredException();
    }

    jwtService.delete(user);

    String accessToken = jwtProvider.generateToken(user);
    String refreshToken = jwtProvider.generateRefreshToken(user);

    jwtService.save(user, accessToken, refreshToken, refreshTokenExp);

    log.info("login - 로그인 성공: userId={}, email={}", user.getId(), user.getEmail());

    return new LoginResult(accessToken, refreshToken, user.isTempPassword());
  }

  public TokenPair refresh(String refreshToken) {
    log.info("refresh - 리프레시 토큰 요청");
    return jwtService.reissueTokenPair(refreshToken, refreshTokenExp);
  }

  public Optional<String> reissueAccessToken(String refreshToken) {
    log.info("reissueAccessToken - 액세스 토큰 재발급 요청");
    return jwtService.getAccessTokenByRefreshToken(refreshToken);
  }

  public void invalidateSessionByRefreshToken(String refreshToken, boolean userBlackList) {
    log.info("invalidateSession - 세션 무효화 요청, userBlackList={}", userBlackList);
    jwtService.invalidateSessionByRefreshToken(refreshToken, userBlackList);
  }

  public void resetPassword(String email) {
    log.info("resetPassword - 비밀번호 재설정 시도: email={}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> {
          log.warn("존재하지 않는 사용자: email={}", email);
          return new UserNotFoundException();
        });

    String tempPassword = generateSecureTempPassword();
    String encoded = passwordEncoder.encode(tempPassword);

    User updatedUser = user.toBuilder()
        .password(encoded)
        .isTempPassword(true)
        .tempPasswordExpiredAt(LocalDateTime.now().plusMinutes(tempPasswordExpirationMinutes))
        .build();

    userRepository.save(updatedUser);
    emailService.sendTempPassword(user.getEmail(), tempPassword);

    log.info("resetPassword - 임시 비밀번호 전송 완료: userId={}, email={}", user.getId(), user.getEmail());
  }

  private String generateSecureTempPassword() {
    String temp = "M0pl!" + UUID.randomUUID().toString().substring(0, 4);
    log.info("generateSecureTempPassword - 임시 비밀번호 생성됨");
    return temp;
  }

  public void changePassword(UUID userId, String newPassword) {
    log.info("changePassword - 비밀번호 변경 시도: userId={}", userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> {
          log.warn("존재하지 않는 사용자: userId={}", userId);
          return new UserNotFoundException();
        });

    User updatedUser = user.toBuilder()
        .password(passwordEncoder.encode(newPassword))
        .isTempPassword(false)
        .tempPasswordExpiredAt(null)
        .build();

    userRepository.save(updatedUser);

    log.info("changePassword - 비밀번호 변경 완료: userId={}", userId);
  }
}
