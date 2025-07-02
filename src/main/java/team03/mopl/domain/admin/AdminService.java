package team03.mopl.domain.admin;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
public class AdminService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final JwtSessionRepository jwtSessionRepository;

  @Transactional
  public UserResponse changeRole(UUID userId, Role newRole) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    User updated=user.toBuilder()
        .role(newRole)
        .build();

    userRepository.save(updated);

    jwtService.invalidateSessionByRefreshToken(
        jwtSessionRepository.findFirstByUserId(userId)
            .map(JwtSession::getRefreshToken)
            .orElse(null),
        true
    );
    return UserResponse.from(updated);
  }

  @Transactional
  public UserResponse lockUser(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    User updated=user.toBuilder()
        .isLocked(true)
        .build();

    userRepository.save(updated);
    jwtService.invalidateSessionByRefreshToken(
        jwtSessionRepository.findFirstByUserId(userId)
            .map(JwtSession::getRefreshToken)
            .orElse(null),
        true
    );

    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse unlockUser(UUID userId) {
    User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);

    User updated=user.toBuilder()
        .isLocked(false)
        .build();

    userRepository.save(updated);

    return UserResponse.from(user);
  }

}
