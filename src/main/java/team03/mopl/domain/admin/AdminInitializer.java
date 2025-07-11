package team03.mopl.domain.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import team03.mopl.domain.user.Role;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${admin.email}")
  private String adminEmail;

  @Value("${admin.password}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    if (userRepository.findByEmail(adminEmail).isEmpty()) {
      User admin = User.builder()
          .email(adminEmail)
          .password(passwordEncoder.encode(adminPassword))
          .name("관리자")
          .role(Role.ADMIN)
          .isTempPassword(false)
          .build();

      userRepository.save(admin);
    }
  }
}
