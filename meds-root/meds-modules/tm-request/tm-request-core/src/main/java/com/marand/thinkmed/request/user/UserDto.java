package com.marand.thinkmed.request.user;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Nejc Korasa
 */

public class UserDto
{
  private final String id;
  private final String username;
  private final String fullName;
  private final List<GrantedAuthority> authorities;

  public UserDto(
      final String id,
      final String username,
      final String fullName,
      final Collection<? extends GrantedAuthority> authorities)
  {
    this.id = id;
    this.username = username;
    this.fullName = fullName;
    this.authorities = getSortedAuthorities(Preconditions.checkNotNull(authorities, "authorities"));
  }

  private List<GrantedAuthority> getSortedAuthorities(final Collection<? extends GrantedAuthority> authorities)
  {
    return authorities.stream().distinct().sorted(Comparator.comparing(GrantedAuthority::getAuthority)).collect(Collectors.toList());
  }

  public String getId()
  {
    return id;
  }

  public String getUsername()
  {
    return username;
  }

  public String getFullName()
  {
    return fullName;
  }

  public Collection<GrantedAuthority> getAuthorities()
  {
    return authorities;
  }
}
