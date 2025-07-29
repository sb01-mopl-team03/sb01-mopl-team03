package team03.mopl.jwt;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import team03.mopl.domain.user.User;
import team03.mopl.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetails loadUserById(UUID userId){
    User user = userRepository.findById(userId).orElseThrow(
        () -> new UsernameNotFoundException("User " + userId + " not found"));
    return new CustomUserDetails(user);
  }

  @Override
  public UserDetails loadUserByUsername(String email){
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException(email));
    return new CustomUserDetails(user);
  }
}
