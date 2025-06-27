package team03.mopl.jwt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import team03.mopl.domain.user.User;

public class CustomUserDetails implements UserDetails {

  private User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  public UUID getId() {
    return user.getId();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }

  @Override
  public String getUsername() {
    return user.getEmail();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CustomUserDetails that)) {
      return false;
    }
    return Objects.equals(getUser(), that.getUser());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getUser());
  }
}
