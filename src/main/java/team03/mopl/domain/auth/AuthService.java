package team03.mopl.domain.auth;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.UserResponse;
import team03.mopl.jwt.JwtProvider;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.JwtSession;
import team03.mopl.jwt.JwtSessionRepository;
import team03.mopl.jwt.TokenPair;

@Service
@RequiredArgsConstructor
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
    User user = userRepository.findByEmail(email)
        .orElseThrow(UserNotFoundException::new);

    if(user.isLocked()){
      jwtSessionRepository.deleteByUser(user);
      throw new IllegalStateException("잠긴 계정");
    }

    if(!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalStateException("비밀번호가 일치x");
    }

    if(user.isTempPassword()){
      if (user.getTempPasswordExpiredAt().isBefore(LocalDateTime.now())){
        throw new IllegalStateException("Temporary password expired");
      }
    }

    jwtService.delete(user);

    String accessToken = jwtProvider.generateToken(user);
    String refreshToken = jwtProvider.generateRefreshToken(user);

    jwtService.save(user, accessToken, refreshToken, refreshTokenExp);

    return new LoginResult(accessToken, refreshToken,user.isTempPassword());
  }

  public TokenPair refresh(String refreshToken) {
    return jwtService.reissueTokenPair(refreshToken, refreshTokenExp);
  }

  public Optional<String> reissueAccessToken(String refreshToken) {
    return jwtService.getAccessTokenByRefreshToken(refreshToken);
  }

  public void invalidateSessionByRefreshToken(String refreshToken) {
    jwtService.invalidateSessionByRefreshToken(refreshToken,false);
  }

  public void resetPassword(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

    String tempPassword=generateSecureTempPassword();
    String encoded= passwordEncoder.encode(tempPassword);

    User updatedUser= user.toBuilder()
        .password(encoded)
        .isTempPassword(true)
        .tempPasswordExpiredAt(LocalDateTime.now().plusMinutes(tempPasswordExpirationMinutes))
        .build();

    userRepository.save(updatedUser);
    emailService.sendTempPassword(user.getEmail(),tempPassword);
  }

  private String generateSecureTempPassword() {
    return "M0pl!"+ UUID.randomUUID().toString().substring(0,4);
  }

  public void changePassword(UUID userId, String newPassword) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    User updatedUser=user.toBuilder()
        .password(passwordEncoder.encode(newPassword))
        .isTempPassword(false)
        .tempPasswordExpiredAt(null)
        .build();

    userRepository.save(updatedUser);
  }
}
