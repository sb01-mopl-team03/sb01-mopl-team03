package team03.mopl.jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import team03.mopl.domain.user.User;

public class CustomUserDetails implements UserDetails, OAuth2User {

  private User user;
  private Map<String, Object> attributes;

  public CustomUserDetails(User user) {
    this(user, Map.of());
  }

  public CustomUserDetails(User user, Map<String, Object> attributes) {
    this.user = user;
    this.attributes = attributes;
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
  public Map<String, Object> getAttributes() {
    return attributes;
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
    return Objects.equals(getUser(), that.getUser().getId());
  }

  @Override
  public int hashCode() {
    return user.getId().hashCode();
  }

  @Override
  public String getName() {
    return user.getId().toString();
  }
}
