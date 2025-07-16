package team03.mopl.domain.admin;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team03.mopl.common.exception.user.UserNotFoundException;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;
import team03.mopl.domain.user.UserResponse;
import team03.mopl.jwt.JwtService;
import team03.mopl.jwt.JwtSession;
import team03.mopl.jwt.JwtSessionRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final JwtSessionRepository jwtSessionRepository;

  @Transactional
  public UserResponse changeRole(UUID userId, Role newRole) {
    log.info("changeRole - 권한 변경 시작: userId={}, newRole={}", userId, newRole);

    User user = userRepository.findById(userId).orElseThrow(() -> {
      log.warn("존재하지 않는 사용자: userId={}", userId);
      return new UserNotFoundException();
    });

    User updated = user.toBuilder()
        .role(newRole)
        .build();

    userRepository.save(updated);

    String refreshToken = jwtSessionRepository.findFirstByUserId(userId)
        .map(JwtSession::getRefreshToken)
        .orElse(null);

    jwtService.invalidateSessionByRefreshToken(refreshToken, true);
    log.info("changeRole - 권한 변경 완료: userId={}, newRole={}", userId, newRole);

    return UserResponse.from(updated);
  }

  @Transactional
  public UserResponse lockUser(UUID userId) {
    log.info("lockUser - 계정 잠금 시작: userId={}", userId);

    User user = userRepository.findById(userId).orElseThrow(() -> {
      log.warn("존재하지 않는 사용자: userId={}", userId);
      return new UserNotFoundException();
    });

    User updated = user.toBuilder()
        .isLocked(true)
        .build();

    userRepository.save(updated);

    String refreshToken = jwtSessionRepository.findFirstByUserId(userId)
        .map(JwtSession::getRefreshToken)
        .orElse(null);

    jwtService.invalidateSessionByRefreshToken(refreshToken, true);
    log.info("lockUser - 계정 잠금 완료 및 세션 무효화: userId={}", userId);

    return UserResponse.from(updated);
  }

  @Transactional
  public UserResponse unlockUser(UUID userId) {
    log.info("unlockUser - 계정 잠금 해제 시작: userId={}", userId);

    User user = userRepository.findById(userId).orElseThrow(() -> {
      log.warn("존재하지 않는 사용자: userId={}", userId);
      return new UserNotFoundException();
    });

    User updated = user.toBuilder()
        .isLocked(false)
        .build();

    userRepository.save(updated);

    log.info("unlockUser - 계정 잠금 해제 완료: userId={}", userId);
    return UserResponse.from(updated);
  }

}
